package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.pricing.PricingCalculateRequest;
import ao.angogas.backend.dto.request.pricing.UpdatePlatformSettingsRequest;
import ao.angogas.backend.dto.request.pricing.UpdateProductPricingRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
@Tag(name = "Precificação", description = "Motor de cálculo de preços e parâmetros da plataforma")
public class PricingController {

    private final PricingService pricingService;

    // ── Cliente (autenticado) ──────────────────────────────────────────────

    @PostMapping("/calculate")
    @Operation(summary = "Calcular preço de uma encomenda antes de confirmar",
               description = "Recebe produto, quantidade, addressId e flag de garrafa. " +
                             "Devolve o breakdown completo: produto, taxa base, distância, tempo, comissão e total.")
    public ResponseEntity<ApiResponse<?>> calculate(@Valid @RequestBody PricingCalculateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.calculate(request)));
    }

    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Ver parâmetros globais da plataforma")
    public ResponseEntity<ApiResponse<?>> getSettings() {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.getSettings()));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Actualizar parâmetros globais (preço/km, comissão, etc.)")
    public ResponseEntity<ApiResponse<?>> updateSettings(
            @Valid @RequestBody UpdatePlatformSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                pricingService.updateSettings(request),
                "Parâmetros actualizados com sucesso"));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar configuração de custo/margem por produto")
    public ResponseEntity<ApiResponse<?>> listProductPricing() {
        return ResponseEntity.ok(ApiResponse.ok(pricingService.listProductPricing()));
    }

    @PutMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Criar ou actualizar custo/margem de um produto")
    public ResponseEntity<ApiResponse<?>> upsertProductPricing(
            @Valid @RequestBody UpdateProductPricingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                pricingService.upsertProductPricing(request),
                "Configuração de produto actualizada"));
    }
}
