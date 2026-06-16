package ao.angogas.backend.dto.request.subscription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSubscriptionRequest(
        @NotBlank String plano,
        @NotNull @Positive BigDecimal precoKz,
        @NotNull LocalDate inicio,
        @NotNull LocalDate fim,
        boolean renovacaoAuto
) {}
