package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.auth.LoginRequest;
import ao.angogas.backend.dto.request.auth.RefreshTokenRequest;
import ao.angogas.backend.dto.request.auth.RegisterRequest;
import ao.angogas.backend.dto.response.auth.AuthResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.mapper.UserMapper;
import ao.angogas.backend.model.RefreshToken;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.UserRole;
import ao.angogas.backend.repository.RefreshTokenRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.security.JwtService;
import ao.angogas.backend.service.AuthService;
import ao.angogas.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserService userService;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() == null && request.getTelefone() == null) {
            throw new BusinessException("Email ou telefone é obrigatório");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já registado");
        }
        if (request.getTelefone() != null && userRepository.existsByTelefone(request.getTelefone())) {
            throw new BusinessException("Telefone já registado");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CLIENTE)
                .fotoPerfil(request.getFotoPerfil())
                .build();

        user = userRepository.save(user);

        if (user.getEmail() != null) {
            userService.triggerVerificationEmail(user.getId());
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmailOrTelefone(request.getEmailOrTelefone(), request.getEmailOrTelefone())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isActivo()) {
            throw new UnauthorizedException("Conta desactivada. Contacte o suporte.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenAndRevogadoFalse(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido ou expirado"));

        if (refreshToken.getExpiraEm().isBefore(OffsetDateTime.now())) {
            refreshToken.setRevogado(true);
            refreshTokenRepository.save(refreshToken);
            throw new UnauthorizedException("Refresh token expirado. Faça login novamente.");
        }

        refreshToken.setRevogado(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(refreshToken.getUser());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevogadoFalse(refreshToken)
                .ifPresent(t -> {
                    t.setRevogado(true);
                    refreshTokenRepository.save(t);
                });
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        refreshTokenRepository.revokeAllByUserId(user.getId());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiraEm(OffsetDateTime.now().plusSeconds(refreshExpiration))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(accessExpiration)
                .user(userMapper.toResponse(user))
                .build();
    }
}
