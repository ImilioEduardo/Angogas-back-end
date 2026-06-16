package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.subscription.CreateSubscriptionRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscrições", description = "Gestão de subscrições mensais")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Criar subscrição")
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody CreateSubscriptionRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(subscriptionService.create(request, currentUser), "Subscrição activada"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "A minha subscrição activa")
    public ResponseEntity<ApiResponse<?>> getMy(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getMy(currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Cancelar subscrição")
    public ResponseEntity<ApiResponse<?>> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        subscriptionService.cancel(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null, "Subscrição cancelada"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todas as subscrições")
    public ResponseEntity<ApiResponse<?>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.listAll(pageable)));
    }
}
