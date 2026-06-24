package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.user.UpdateUserRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Utilizadores", description = "Gestão de perfil e utilizadores")
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    @Operation(summary = "Ver o meu perfil")
    public ResponseEntity<ApiResponse<?>> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getMe(currentUser)));
    }

    @PutMapping("/users/me")
    @Operation(summary = "Actualizar o meu perfil")
    public ResponseEntity<ApiResponse<?>> updateMe(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.update(currentUser.getId(), request)));
    }

    @GetMapping("/users/verify-email")
    @Operation(summary = "Verificar email com token")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.ok(userService.verifyEmail(token), "Email verificado com sucesso"));
    }

    @PostMapping("/users/me/resend-verification")
    @Operation(summary = "Reenviar email de verificação")
    public ResponseEntity<ApiResponse<?>> resendVerification(@AuthenticationPrincipal User currentUser) {
        userService.triggerVerificationEmail(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Email de verificação reenviado"));
    }

    // --- Admin ---

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todos os utilizadores")
    public ResponseEntity<ApiResponse<?>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(userService.listAll(pageable)));
    }

    @GetMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Ver utilizador por ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id)));
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Desactivar utilizador")
    public ResponseEntity<ApiResponse<?>> deactivate(@PathVariable UUID id) {
        userService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Utilizador desactivado"));
    }
}
