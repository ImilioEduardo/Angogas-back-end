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
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.UserRole;
import ao.angogas.backend.repository.AddressRepository;
import ao.angogas.backend.repository.OrderRepository;
import ao.angogas.backend.repository.ProductRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request, User currentUser) {
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));

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

        Order order = Order.builder()
                .cliente(currentUser)
                .address(address)
                .metodoPagamento(request.getMetodoPagamento())
                .totalKz(total)
                .notas(request.getNotas())
                .status(OrderStatus.PENDENTE)
                .build();

        items.forEach(item -> item.setOrder(order));
        order.getItems().addAll(items);

        return orderMapper.toResponse(orderRepository.save(order));
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

        return orderMapper.toResponse(orderRepository.save(order));
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

        if (order.getStatus() != OrderStatus.PENDENTE && !isAdmin) {
            throw new BusinessException("Só é possível cancelar pedidos no estado PENDENTE");
        }

        if (order.getStatus() == OrderStatus.ENTREGUE) {
            throw new BusinessException("Pedido já entregue — não pode ser cancelado");
        }

        // Devolve stock
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantidade());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
    }
}
