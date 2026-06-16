package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.delivery.AssignZoneRequest;
import ao.angogas.backend.dto.request.delivery.CreateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryStatusRequest;
import ao.angogas.backend.dto.request.delivery.UpdateLocationRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.DeliveryAgentService;
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
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Entregador", description = "Painel do entregador e gestão de entregas")
public class DeliveryAgentController {

    private final DeliveryAgentService deliveryAgentService;

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Listar os meus pedidos do dia")
    public ResponseEntity<ApiResponse<?>> listMyOrders(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(deliveryAgentService.listMyOrders(currentUser, pageable)));
    }

    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Actualizar status de entrega")
    public ResponseEntity<ApiResponse<?>> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryAgentService.updateDeliveryStatus(orderId, request, currentUser),
                "Status actualizado"));
    }

    @PostMapping("/orders/{orderId}/location")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Actualizar localização GPS")
    public ResponseEntity<ApiResponse<?>> updateLocation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateLocationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryAgentService.updateLocation(orderId, request, currentUser)));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Ver o meu perfil de entregador")
    public ResponseEntity<ApiResponse<?>> getMyProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryAgentService.getMyProfile(currentUser)));
    }

    @PatchMapping("/disponivel")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Toggle disponibilidade")
    public ResponseEntity<ApiResponse<?>> toggleDisponivel(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryAgentService.toggleDisponivel(currentUser)));
    }

    // --- Admin ---

    @PostMapping("/admin/agents")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Criar entregador")
    public ResponseEntity<ApiResponse<?>> createAgent(@Valid @RequestBody CreateDeliveryAgentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(deliveryAgentService.createAgent(request), "Entregador criado com sucesso"));
    }

    @GetMapping("/admin/agents")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todos os entregadores")
    public ResponseEntity<ApiResponse<?>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(deliveryAgentService.listAll(pageable)));
    }

    @PutMapping("/admin/agents/{agentId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Actualizar dados de entregador")
    public ResponseEntity<ApiResponse<?>> updateAgent(
            @PathVariable UUID agentId,
            @Valid @RequestBody UpdateDeliveryAgentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryAgentService.updateAgent(agentId, request), "Entregador actualizado"));
    }

    @PutMapping("/admin/agents/{agentId}/zone")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Atribuir zona a entregador")
    public ResponseEntity<ApiResponse<?>> assignZone(
            @PathVariable UUID agentId,
            @Valid @RequestBody AssignZoneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryAgentService.assignZone(agentId, request), "Zona atribuída"));
    }
}
