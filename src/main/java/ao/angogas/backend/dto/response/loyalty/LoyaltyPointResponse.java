package ao.angogas.backend.dto.response.loyalty;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LoyaltyPointResponse(
        UUID id,
        int pontos,
        String motivo,
        UUID orderId,
        OffsetDateTime criadoEm
) {}
