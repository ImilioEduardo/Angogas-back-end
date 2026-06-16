package ao.angogas.backend.dto.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLocationRequest {
    @NotNull(message = "Latitude é obrigatória")
    private BigDecimal latitude;

    @NotNull(message = "Longitude é obrigatória")
    private BigDecimal longitude;
}
