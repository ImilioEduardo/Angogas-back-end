package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.pricing.PricingCalculateRequest;
import ao.angogas.backend.dto.request.pricing.UpdatePlatformSettingsRequest;
import ao.angogas.backend.dto.request.pricing.UpdateProductPricingRequest;
import ao.angogas.backend.dto.response.pricing.PlatformSettingsResponse;
import ao.angogas.backend.dto.response.pricing.PricingBreakdownResponse;
import ao.angogas.backend.dto.response.pricing.ProductPricingResponse;
import ao.angogas.backend.model.Order;

import java.util.List;

public interface PricingService {

    /**
     * Calcula o breakdown completo de preço para uma encomenda antes de a confirmar.
     * Toda a lógica de negócio (fórmula §4 do documento) vive aqui — o frontend
     * apenas envia parâmetros e exibe o resultado.
     */
    PricingBreakdownResponse calculate(PricingCalculateRequest request);

    /** Devolve os parâmetros globais da plataforma (singleton). */
    PlatformSettingsResponse getSettings();

    /** Actualiza os parâmetros globais (ADMIN only). */
    PlatformSettingsResponse updateSettings(UpdatePlatformSettingsRequest request);

    /** Lista a configuração de custo/margem de todos os produtos. */
    List<ProductPricingResponse> listProductPricing();

    /** Cria ou actualiza a configuração de preço de um produto (ADMIN only). */
    ProductPricingResponse upsertProductPricing(UpdateProductPricingRequest request);

    /**
     * Regista o resumo financeiro de uma entrega concluída.
     * Chamado automaticamente quando o status do pedido passa a ENTREGUE.
     * Idempotente: se já existir um registo para o pedido, não faz nada.
     */
    void recordDeliveryFinancials(Order order);
}
