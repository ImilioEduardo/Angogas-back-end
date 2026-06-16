package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.auth.LoginRequest;
import ao.angogas.backend.dto.request.auth.RefreshTokenRequest;
import ao.angogas.backend.dto.request.auth.RegisterRequest;
import ao.angogas.backend.dto.response.auth.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
}
