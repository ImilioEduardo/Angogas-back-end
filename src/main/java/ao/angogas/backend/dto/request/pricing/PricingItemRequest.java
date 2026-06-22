package ao.angogas.backend.dto.request.pricing;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class PricingItemRequest {

    @NotNull(message = "Produto obrigatório")
    private UUID produtoId;

    @Min(value = 1, message = "Quantidade mínima é 1")
    @Max(value = 15, message = "Quantidade máxima é 15 por tipo")
    private int quantidade;
}
