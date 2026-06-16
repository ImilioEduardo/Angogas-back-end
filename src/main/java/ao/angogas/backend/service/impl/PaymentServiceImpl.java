package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.response.payment.PaymentResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.Payment;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.PaymentMethod;
import ao.angogas.backend.model.enums.PaymentStatus;
import ao.angogas.backend.repository.OrderRepository;
import ao.angogas.backend.repository.PaymentRepository;
import ao.angogas.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public PaymentResponse confirmCashPayment(UUID orderId, User entregador) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getEntregador() == null || !order.getEntregador().getId().equals(entregador.getId())) {
            throw new UnauthorizedException("Este pedido não está atribuído a ti");
        }

        if (paymentRepository.findByOrderId(orderId).isPresent()) {
            throw new BusinessException("Pagamento já registado para este pedido");
        }

        Payment payment = Payment.builder()
                .order(order)
                .metodo(PaymentMethod.DINHEIRO)
                .status(PaymentStatus.APROVADO)
                .valorKz(order.getTotalKz())
                .confirmadoEm(OffsetDateTime.now())
                .build();

        return toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado para este pedido"));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrder().getId(),
                p.getMetodo(),
                p.getStatus(),
                p.getReferenciaExterna(),
                p.getValorKz(),
                p.getCriadoEm(),
                p.getConfirmadoEm()
        );
    }
}
