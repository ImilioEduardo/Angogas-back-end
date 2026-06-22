package ao.angogas.backend.dto.request.pricing;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePlatformSettingsRequest {

    @NotNull @DecimalMin("150.00") @DecimalMax("600.00")
    private BigDecimal precoPorKm;

    @NotNull @DecimalMin("30.00") @DecimalMax("120.00")
    private BigDecimal precoPorMinuto;

    /** 0.05 – 0.20 (5 % a 20 %) */
    @NotNull @DecimalMin("0.05") @DecimalMax("0.20")
    private BigDecimal comissaoApp;

    @NotNull @DecimalMin("1.00") @DecimalMax("5.00")
    private BigDecimal distanciaMinKm;

    @NotNull @DecimalMin("10.00") @DecimalMax("100.00")
    private BigDecimal distanciaMaxKm;

    /** 0.15 – 0.40 (15 % a 40 %) */
    @NotNull @DecimalMin("0.15") @DecimalMax("0.40")
    private BigDecimal margemEntrega;

    @NotNull @Min(1) @Max(15)
    private Integer quantidadeMaxBotijas;

    @NotNull @DecimalMin("0.00")
    private BigDecimal custofixoMensal;

    @NotNull @Min(1)
    private Integer entregasEstimadasMes;

    @NotNull
    private BigDecimal armazemLatitude;

    @NotNull
    private BigDecimal armazemLongitude;

    @NotNull @DecimalMin("10.00") @DecimalMax("120.00")
    private BigDecimal velocidadeMediaKmh;
}
