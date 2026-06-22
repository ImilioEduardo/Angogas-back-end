package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.order.CreateOrderItemRequest;
import ao.angogas.backend.dto.request.order.CreateOrderRequest;
import ao.angogas.backend.dto.request.order.UpdateOrderStatusRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.mapper.OrderMapper;
import ao.angogas.backend.model.*;
import ao.angogas.backend.model.enums.NotificationType;
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.UserRole;
import ao.angogas.backend.repository.*;
import ao.angogas.backend.service.LoyaltyPointService;
import ao.angogas.backend.service.NotificationService;
import ao.angogas.backend.service.OrderService;
import ao.angogas.backend.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final DeliveryAgentRepository deliveryAgentRepository;
    private final NotificationService notificationService;
    private final PricingService pricingService;
    private final LoyaltyPointService loyaltyPointService;
    private final LoyaltyPointRepository loyaltyPointRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request, User currentUser) {
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona não encontrada"));

        if (!zone.isActiva()) {
            throw new BusinessException("Zona de entrega inactiva");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produto não encontrado: " + itemReq.getProductId()));

            if (!product.isActivo()) {
                throw new BusinessException("Produto indisponível: " + product.getNome());
            }
            if (product.getStock() < itemReq.getQuantidade()) {
                throw new BusinessException("Stock insuficiente para: " + product.getNome());
            }

            product.setStock(product.getStock() - itemReq.getQuantidade());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrecoKz().multiply(BigDecimal.valueOf(itemReq.getQuantidade()));
            total = total.add(subtotal);

            items.add(OrderItem.builder()
                    .product(product)
                    .quantidade(itemReq.getQuantidade())
                    .precoUnitario(product.getPrecoKz())
                    .build());
        }

        BigDecimal descontoPontos = BigDecimal.ZERO;
        if (request.isUsarPontos()) {
            Integer saldo = loyaltyPointRepository.sumPontosByUserId(currentUser.getId());
            int pts = (saldo != null && saldo > 0) ? saldo : 0;
            if (pts > 0) {
                descontoPontos = BigDecimal.valueOf(Math.min(pts, total.intValue()));
                total = total.subtract(descontoPontos);
            }
        }

        Order order = Order.builder()
                .cliente(currentUser)
                .address(address)
                .zone(zone)
                .metodoPagamento(request.getMetodoPagamento())
                .totalKz(total)
                .descontoPontos(descontoPontos)
                .notas(request.getNotas())
                .status(OrderStatus.AGUARDANDO_ACEITACAO)
                .codigoEntrega(generateDeliveryCode())
                .build();

        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        Order saved = orderRepository.save(order);

        if (descontoPontos.compareTo(BigDecimal.ZERO) > 0) {
            loyaltyPointService.addPoints(
                    currentUser.getId(),
                    -descontoPontos.intValue(),
                    "Desconto no pedido #" + saved.getId().toString().substring(0, 8).toUpperCase(),
                    saved.getId()
            );
        }

        notifyAgentsInZone(zone, saved);

        return orderMapper.toResponse(saved);
    }

    private String generateDeliveryCode() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void notifyAgentsInZone(Zone zone, Order order) {
        List<DeliveryAgent> agents = deliveryAgentRepository.findByZoneIdAndDisponivelTrue(zone.getId());
        for (DeliveryAgent agent : agents) {
            notificationService.send(
                    agent.getUser().getId(),
                    "Novo pedido na tua zona",
                    "Pedido #" + order.getId().toString().substring(0, 8).toUpperCase()
                            + " em " + zone.getNome() + ". Aceita ou recusa.",
                    NotificationType.PEDIDO
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(UUID id, User currentUser) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (currentUser.getRole() != UserRole.ADMIN
                && !order.getCliente().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Acesso negado a este pedido");
        }

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listByClient(User currentUser, Pageable pageable) {
        Page<UUID> idPage = orderRepository.findIdsByClienteId(currentUser.getId(), pageable);
        List<Order> orders = orderRepository.findAllWithAssociationsByIdIn(idPage.getContent());
        return buildPageResponse(idPage, orders, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listAll(String status, Pageable pageable) {
        Page<UUID> idPage;
        if (status != null && !status.isBlank()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            idPage = orderRepository.findIdsByStatus(orderStatus, pageable);
        } else {
            idPage = orderRepository.findAllIds(pageable);
        }
        List<Order> orders = orderRepository.findAllWithAssociationsByIdIn(idPage.getContent());
        return buildPageResponse(idPage, orders, pageable);
    }

    private PageResponse<OrderResponse> buildPageResponse(Page<UUID> idPage, List<Order> orders, Pageable pageable) {
        org.springframework.data.domain.Page<OrderResponse> responsePage =
                new org.springframework.data.domain.PageImpl<>(
                        orders.stream().map(orderMapper::toResponse).toList(),
                        pageable,
                        idPage.getTotalElements());
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getStatus() == OrderStatus.ENTREGUE || order.getStatus() == OrderStatus.CANCELADO) {
            throw new BusinessException("Pedido já finalizado — status não pode ser alterado");
        }

        order.setStatus(request.getStatus());

        if (request.getEntregadorId() != null) {
            User entregador = userRepository.findById(request.getEntregadorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));
            if (entregador.getRole() != UserRole.ENTREGADOR) {
                throw new BusinessException("Utilizador não é um entregador");
            }
            order.setEntregador(entregador);
        }

        Order saved = orderRepository.save(order);

        if (request.getStatus() == OrderStatus.ENTREGUE) {
            pricingService.recordDeliveryFinancials(saved);
        }

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cancel(UUID id, User currentUser) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        boolean isOwner = order.getCliente().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new UnauthorizedException("Acesso negado");
        }

        if (order.getStatus() != OrderStatus.PENDENTE
                && order.getStatus() != OrderStatus.AGUARDANDO_ACEITACAO
                && !isAdmin) {
            throw new BusinessException("Só é possível cancelar pedidos no estado PENDENTE ou AGUARDANDO_ACEITACAO");
        }

        if (order.getStatus() == OrderStatus.ENTREGUE) {
            throw new BusinessException("Pedido já entregue — não pode ser cancelado");
        }

        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantidade());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public OrderResponse accept(UUID id, User entregador) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getStatus() != OrderStatus.AGUARDANDO_ACEITACAO) {
            throw new BusinessException("Este pedido já foi aceite ou não está disponível");
        }

        order.setEntregador(entregador);
        order.setStatus(OrderStatus.CONFIRMADO);
        Order saved = orderRepository.save(order);

        notificationService.send(
                order.getCliente().getId(),
                "Pedido aceite!",
                "O teu pedido #" + id.toString().substring(0, 8).toUpperCase()
                        + " foi aceite por " + entregador.getNome() + " e está a ser preparado.",
                NotificationType.ENTREGA
        );

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void reject(UUID id, User entregador) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getStatus() != OrderStatus.AGUARDANDO_ACEITACAO) {
            throw new BusinessException("Este pedido já não está disponível para recusa");
        }
    }
}
