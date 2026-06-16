package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.loyalty.AddPointsRequest;
import ao.angogas.backend.dto.request.loyalty.RedeemPointsRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.LoyaltyPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
@Tag(name = "Fidelidade", description = "Programa de pontos de fidelidade")
public class LoyaltyController {

    private final LoyaltyPointService loyaltyPointService;

    @GetMapping("/balance")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Ver saldo e histórico de pontos")
    public ResponseEntity<ApiResponse<?>> getBalance(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyPointService.getBalance(currentUser)));
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Gastar pontos")
    public ResponseEntity<ApiResponse<?>> redeem(
            @Valid @RequestBody RedeemPointsRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(
                loyaltyPointService.redeem(request, currentUser), "Pontos utilizados com sucesso"));
    }

    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Adicionar pontos a um utilizador")
    public ResponseEntity<ApiResponse<?>> addManual(@Valid @RequestBody AddPointsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                loyaltyPointService.addManual(request), "Pontos adicionados"));
    }
}
