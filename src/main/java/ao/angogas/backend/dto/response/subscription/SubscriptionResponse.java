package ao.angogas.backend.dto.response.subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID userId,
        String plano,
        BigDecimal precoKz,
        LocalDate inicio,
        LocalDate fim,
        boolean activa,
        boolean renovacaoAuto,
        OffsetDateTime criadoEm
) {}
