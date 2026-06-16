package ao.angogas.backend.dto.request.loyalty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddPointsRequest(
        @NotNull UUID userId,
        @NotNull @Positive int pontos,
        @NotBlank String motivo
) {}
