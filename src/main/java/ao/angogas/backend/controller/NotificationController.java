package ao.angogas.backend.controller;

import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações", description = "Gestão de notificações do utilizador")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Listar as minhas notificações")
    public ResponseEntity<ApiResponse<?>> listMy(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(notificationService.listMy(currentUser, pageable)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Contar notificações não lidas")
    public ResponseEntity<ApiResponse<?>> countUnread(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.countUnread(currentUser)));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<ApiResponse<?>> markRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        notificationService.markRead(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null, "Notificação marcada como lida"));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Marcar todas as notificações como lidas")
    public ResponseEntity<ApiResponse<?>> markAllRead(@AuthenticationPrincipal User currentUser) {
        notificationService.markAllRead(currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null, "Todas as notificações marcadas como lidas"));
    }
}
