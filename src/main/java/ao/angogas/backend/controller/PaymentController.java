package ao.angogas.backend.controller;

import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamentos", description = "Gestão de pagamentos")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/cash/confirm/{orderId}")
    @PreAuthorize("hasRole('ENTREGADOR')")
    @Operation(summary = "Confirmar pagamento em dinheiro")
    public ResponseEntity<ApiResponse<?>> confirmCash(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                paymentService.confirmCashPayment(orderId, currentUser), "Pagamento confirmado"));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','ENTREGADOR','CLIENTE')")
    @Operation(summary = "Ver pagamento de um pedido")
    public ResponseEntity<ApiResponse<?>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getByOrderId(orderId)));
    }
}
