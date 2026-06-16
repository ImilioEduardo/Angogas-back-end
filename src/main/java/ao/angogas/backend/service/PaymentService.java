package ao.angogas.backend.service;

import ao.angogas.backend.dto.response.payment.PaymentResponse;
import ao.angogas.backend.model.User;

import java.util.UUID;

public interface PaymentService {
    PaymentResponse confirmCashPayment(UUID orderId, User entregador);
    PaymentResponse getByOrderId(UUID orderId);
}
