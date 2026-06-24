package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.user.UpdateUserRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.user.UserResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.mapper.UserMapper;
import ao.angogas.backend.model.EmailVerificationToken;
import ao.angogas.backend.model.User;
import ao.angogas.backend.repository.EmailVerificationTokenRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.service.EmailService;
import ao.angogas.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public UserResponse getMe(User currentUser) {
        return userMapper.toResponse(currentUser);
    }

    @Override
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email já está em uso");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerificado(false);
        }

        if (request.getTelefone() != null && !request.getTelefone().equals(user.getTelefone())) {
            if (userRepository.existsByTelefone(request.getTelefone())) {
                throw new BusinessException("Telefone já está em uso");
            }
            user.setTelefone(request.getTelefone());
        }

        if (request.getNome() != null) {
            user.setNome(request.getNome());
        }

        if (request.getFotoPerfil() != null && !request.getFotoPerfil().isBlank()) {
            user.setFotoPerfil(request.getFotoPerfil());
        }

        User saved = userRepository.save(user);

        if (request.getEmail() != null && !request.getEmail().isBlank() && !saved.isEmailVerificado()) {
            triggerVerificationEmail(saved.getId());
        }

        return userMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void triggerVerificationEmail(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }
        if (user.isEmailVerificado()) {
            return;
        }

        tokenRepository.deleteByUserId(userId);

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiraEm(OffsetDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getNome(), verificationToken.getToken());
    }

    @Override
    @Transactional
    public UserResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository
                .findByTokenAndUsadoFalse(token)
                .orElseThrow(() -> new BusinessException("Link de verificação inválido ou já utilizado"));

        if (verificationToken.getExpiraEm().isBefore(OffsetDateTime.now())) {
            throw new BusinessException("O link de verificação expirou. Solicita um novo.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerificado(true);
        userRepository.save(user);

        verificationToken.setUsado(true);
        tokenRepository.save(verificationToken);

        return userMapper.toResponse(user);
    }

    @Override
    public PageResponse<UserResponse> listAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(userMapper::toResponse));
    }

    @Override
    public UserResponse getById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));
        user.setActivo(false);
        userRepository.save(user);
    }
}
