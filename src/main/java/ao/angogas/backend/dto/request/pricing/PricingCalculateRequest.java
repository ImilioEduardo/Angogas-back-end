package ao.angogas.backend.dto.request.pricing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PricingCalculateRequest {

    /**
     * Multi-produto: lista de itens (tem prioridade sobre os campos legacy abaixo).
     */
    private List<@Valid PricingItemRequest> items;

    // ── Campos legacy (compatibilidade com clientes mais antigos) ────────

    private UUID produtoId;

    @Min(value = 1, message = "Quantidade mínima é 1")
    @Max(value = 15, message = "Quantidade máxima é 15")
    private int quantidade = 1;

    // ── Comuns ────────────────────────────────────────────────────────────

    private UUID addressId;

    private boolean clienteTemGarrafa = true;

    /**
     * Devolve a lista de itens efectiva:
     *  - se `items` não for nulo/vazio, usa-a directamente;
     *  - caso contrário, constrói um item singular a partir dos campos legacy.
     */
    public List<PricingItemRequest> resolvedItems() {
        if (items != null && !items.isEmpty()) {
            return items;
        }
        if (produtoId != null) {
            PricingItemRequest single = new PricingItemRequest();
            single.setProdutoId(produtoId);
            single.setQuantidade(quantidade);
            return List.of(single);
        }
        return List.of();
    }
}
