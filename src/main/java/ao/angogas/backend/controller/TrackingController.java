package ao.angogas.backend.controller;

import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.DeliveryAgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
@Tag(name = "Rastreio", description = "Rastreio em tempo real de entregas")
public class TrackingController {

    private final DeliveryAgentService deliveryAgentService;

    @GetMapping("/orders/{orderId}/location")
    @Operation(summary = "Última localização do entregador para este pedido")
    public ResponseEntity<ApiResponse<?>> getLastLocation(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(deliveryAgentService.getLastLocation(orderId)));
    }

    @GetMapping("/orders/{orderId}/history")
    @Operation(summary = "Histórico completo de pontos GPS de um pedido concluído")
    public ResponseEntity<ApiResponse<?>> getTrackingHistory(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                deliveryAgentService.getTrackingHistory(orderId, currentUser)));
    }
}
