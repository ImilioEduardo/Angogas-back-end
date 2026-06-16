package ao.angogas.backend.dto.request.payment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ConfirmCashPaymentRequest(
        @NotNull UUID orderId
) {}
