package ao.angogas.backend.dto.response.payment;

import ao.angogas.backend.model.enums.PaymentMethod;
import ao.angogas.backend.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentMethod metodo,
        PaymentStatus status,
        String referenciaExterna,
        BigDecimal valorKz,
        OffsetDateTime criadoEm,
        OffsetDateTime confirmadoEm
) {}
