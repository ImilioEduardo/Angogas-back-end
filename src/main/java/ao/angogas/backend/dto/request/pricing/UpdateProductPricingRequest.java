package ao.angogas.backend.dto.request.pricing;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductPricingRequest {

    @NotNull(message = "Produto obrigatório")
    private UUID produtoId;

    @NotNull @DecimalMin("0.00")
    private BigDecimal custoSonagazKz;

    /** 0.00 – 1.00 */
    @NotNull @DecimalMin("0.00") @DecimalMax("1.00")
    private BigDecimal margemProduto;

    @NotNull @DecimalMin("0.00")
    private BigDecimal depositoGarrafaKz;
}
