package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.review.CreateReviewRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.review.ReviewResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.model.DeliveryAgent;
import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.Review;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.NotificationType;
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.repository.DeliveryAgentRepository;
import ao.angogas.backend.repository.OrderRepository;
import ao.angogas.backend.repository.ReviewRepository;
import ao.angogas.backend.service.NotificationService;
import ao.angogas.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final DeliveryAgentRepository deliveryAgentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponse create(CreateReviewRequest request, User currentUser) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (!order.getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Só o cliente do pedido pode avaliar");
        }
        if (order.getStatus() != OrderStatus.ENTREGUE) {
            throw new BusinessException("Só é possível avaliar pedidos entregues");
        }
        if (order.getEntregador() == null) {
            throw new BusinessException("Este pedido não tem entregador atribuído");
        }
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BusinessException("Este pedido já foi avaliado");
        }

        Review review = Review.builder()
                .order(order)
                .cliente(currentUser)
                .agent(order.getEntregador())
                .nota(request.getNota())
                .comentario(request.getComentario())
                .build();
        reviewRepository.save(review);

        // Actualiza média e total do agente
        DeliveryAgent agent = deliveryAgentRepository.findByUserId(order.getEntregador().getId())
                .orElse(null);
        if (agent != null) {
            Double media = reviewRepository.calcularMediaByAgentId(order.getEntregador().getId());
            agent.setAvaliacaoMedia(BigDecimal.valueOf(media).setScale(2, RoundingMode.HALF_UP));
            agent.setTotalEntregas(agent.getTotalEntregas() + 1);
            deliveryAgentRepository.save(agent);
        }

        notificationService.send(
                order.getEntregador().getId(),
                "Nova avaliação recebida",
                currentUser.getNome() + " avaliou a tua entrega com " + request.getNota() + " estrelas",
                NotificationType.AVALIACAO,
                order.getId(),
                "/historico"
        );

        return toResponse(review);
    }

    @Override
    public PageResponse<ReviewResponse> listByAgent(UUID agentId, Pageable pageable) {
        return PageResponse.from(
                reviewRepository.findByAgentId(agentId, pageable).map(this::toResponse));
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .orderId(review.getOrder().getId())
                .nomeCliente(review.getCliente().getNome())
                .nomeAgent(review.getAgent().getNome())
                .nota(review.getNota())
                .comentario(review.getComentario())
                .criadoEm(review.getCriadoEm())
                .build();
    }
}
