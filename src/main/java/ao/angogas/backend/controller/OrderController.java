package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.order.CreateOrderRequest;
import ao.angogas.backend.dto.request.order.UpdateOrderStatusRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.OrderService;
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
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Criação e gestão de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Criar pedido")
    public ResponseEntity<ApiResponse<?>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(orderService.create(request, currentUser), "Pedido criado com sucesso"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver pedido por ID")
    public ResponseEntity<ApiResponse<?>> getById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getById(id, currentUser)));
    }

    @GetMapping("/my")
    @Operation(summary = "Listar os meus pedidos")
    public ResponseEntity<ApiResponse<?>> listMy(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(orderService.listByClient(currentUser, pageable)));
    }

    @DeleteMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<ApiResponse<?>> cancel(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        orderService.cancel(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null, "Pedido cancelado"));
    }

    // --- Admin ---

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todos os pedidos")
    public ResponseEntity<ApiResponse<?>> listAll(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(orderService.listAll(status, pageable)));
    }

    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Actualizar status e/ou atribuir entregador")
    public ResponseEntity<ApiResponse<?>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.updateStatus(id, request)));
    }
}
