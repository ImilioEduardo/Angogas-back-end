package ao.angogas.backend.dto.request.loyalty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RedeemPointsRequest(
        @NotNull @Positive int pontos,
        @NotBlank String motivo
) {}
