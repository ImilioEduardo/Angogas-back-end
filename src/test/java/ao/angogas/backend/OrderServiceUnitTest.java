package ao.angogas.backend;

import ao.angogas.backend.dto.request.order.CreateOrderItemRequest;
import ao.angogas.backend.dto.request.order.CreateOrderRequest;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.mapper.OrderMapper;
import ao.angogas.backend.model.*;
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.PaymentMethod;
import ao.angogas.backend.model.enums.UserRole;
import ao.angogas.backend.repository.*;
import ao.angogas.backend.service.NotificationService;
import ao.angogas.backend.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private DeliveryAgentRepository deliveryAgentRepository;
    @Mock private NotificationService notificationService;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User clienteUser;
    private User entregadorUser;
    private Address address;
    private Zone zone;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        clienteUser = User.builder()
                .id(UUID.randomUUID())
                .nome("Cliente")
                .telefone("+244900000001")
                .role(UserRole.CLIENTE)
                .activo(true)
                .build();

        entregadorUser = User.builder()
                .id(UUID.randomUUID())
                .nome("Entregador")
                .telefone("+244900000002")
                .role(UserRole.ENTREGADOR)
                .activo(true)
                .build();

        zone = Zone.builder()
                .id(UUID.randomUUID())
                .nome("Talatona")
                .municipio("Luanda")
                .activa(true)
                .build();

        address = Address.builder()
                .id(UUID.randomUUID())
                .bairro("Talatona")
                .municipio("Luanda")
                .build();

        product = Product.builder()
                .id(UUID.randomUUID())
                .nome("Botijão 13kg")
                .precoKz(BigDecimal.valueOf(5000))
                .stock(10)
                .activo(true)
                .build();

        order = Order.builder()
                .id(UUID.randomUUID())
                .cliente(clienteUser)
                .address(address)
                .zone(zone)
                .status(OrderStatus.AGUARDANDO_ACEITACAO)
                .totalKz(BigDecimal.valueOf(5000))
                .items(new ArrayList<>())
                .build();
    }

    // ── create() ─────────────────────────────────────────────────────────────

    @Test
    void create_validRequest_returnsResponse() {
        var itemReq = new CreateOrderItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantidade(2);

        var req = new CreateOrderRequest();
        req.setAddressId(address.getId());
        req.setZoneId(zone.getId());
        req.setMetodoPagamento(PaymentMethod.DINHEIRO);
        req.setItems(List.of(itemReq));

        when(addressRepository.findByIdAndUserId(address.getId(), clienteUser.getId()))
                .thenReturn(Optional.of(address));
        when(zoneRepository.findById(zone.getId())).thenReturn(Optional.of(zone));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenReturn(product);
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().build());
        when(deliveryAgentRepository.findByZoneIdAndDisponivelTrue(zone.getId()))
                .thenReturn(List.of());

        OrderResponse result = orderService.create(req, clienteUser);

        assertThat(result).isNotNull();
        verify(productRepository, times(1)).save(product);
        verify(orderRepository, times(1)).save(any(Order.class));
        // Stock decremented
        assertThat(product.getStock()).isEqualTo(8);
    }

    @Test
    void create_addressNotFound_throwsResourceNotFoundException() {
        var req = new CreateOrderRequest();
        req.setAddressId(UUID.randomUUID());
        req.setZoneId(zone.getId());
        req.setMetodoPagamento(PaymentMethod.DINHEIRO);
        req.setItems(List.of());

        when(addressRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(req, clienteUser))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_inactiveZone_throwsBusinessException() {
        Zone inactiveZone = Zone.builder().id(UUID.randomUUID()).nome("X").municipio("X").activa(false).build();
        var req = new CreateOrderRequest();
        req.setAddressId(address.getId());
        req.setZoneId(inactiveZone.getId());
        req.setMetodoPagamento(PaymentMethod.DINHEIRO);
        req.setItems(List.of());

        when(addressRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(address));
        when(zoneRepository.findById(inactiveZone.getId())).thenReturn(Optional.of(inactiveZone));

        assertThatThrownBy(() -> orderService.create(req, clienteUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void create_insufficientStock_throwsBusinessException() {
        product.setStock(1);
        var itemReq = new CreateOrderItemRequest();
        itemReq.setProductId(product.getId());
        itemReq.setQuantidade(5);

        var req = new CreateOrderRequest();
        req.setAddressId(address.getId());
        req.setZoneId(zone.getId());
        req.setMetodoPagamento(PaymentMethod.DINHEIRO);
        req.setItems(List.of(itemReq));

        when(addressRepository.findByIdAndUserId(any(), any())).thenReturn(Optional.of(address));
        when(zoneRepository.findById(zone.getId())).thenReturn(Optional.of(zone));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(req, clienteUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
    }

    // ── getById() ─────────────────────────────────────────────────────────────

    @Test
    void getById_ownerAccess_returnsResponse() {
        when(orderRepository.findByIdWithItems(order.getId())).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().build());

        OrderResponse result = orderService.getById(order.getId(), clienteUser);

        assertThat(result).isNotNull();
    }

    @Test
    void getById_unauthorizedUser_throwsUnauthorizedException() {
        User other = User.builder().id(UUID.randomUUID()).role(UserRole.CLIENTE).build();
        when(orderRepository.findByIdWithItems(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getById(order.getId(), other))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getById_admin_canAccessAnyOrder() {
        User admin = User.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).build();
        when(orderRepository.findByIdWithItems(order.getId())).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().build());

        OrderResponse result = orderService.getById(order.getId(), admin);
        assertThat(result).isNotNull();
    }

    // ── cancel() ──────────────────────────────────────────────────────────────

    @Test
    void cancel_aguardandoOrder_restoresStock() {
        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantidade(2)
                .precoUnitario(BigDecimal.valueOf(5000))
                .build();
        item.setOrder(order);
        order.getItems().add(item);
        product.setStock(3);

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(productRepository.save(product)).thenReturn(product);
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancel(order.getId(), clienteUser);

        assertThat(product.getStock()).isEqualTo(5); // 3 + 2 returned
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);
    }

    @Test
    void cancel_deliveredOrder_throwsBusinessException() {
        order.setStatus(OrderStatus.ENTREGUE);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(order.getId(), clienteUser))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void cancel_byAnotherClient_throwsUnauthorizedException() {
        User other = User.builder().id(UUID.randomUUID()).role(UserRole.CLIENTE).build();
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancel(order.getId(), other))
                .isInstanceOf(UnauthorizedException.class);
    }

    // ── accept() ─────────────────────────────────────────────────────────────

    @Test
    void accept_aguardandoOrder_movesToConfirmado() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(OrderResponse.builder().build());
        doNothing().when(notificationService).send(any(), any(), any(), any());

        OrderResponse result = orderService.accept(order.getId(), entregadorUser);

        assertThat(result).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        assertThat(order.getEntregador()).isEqualTo(entregadorUser);
    }

    @Test
    void accept_alreadyAccepted_throwsBusinessException() {
        order.setStatus(OrderStatus.CONFIRMADO);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.accept(order.getId(), entregadorUser))
                .isInstanceOf(BusinessException.class);
    }

    // ── reject() ─────────────────────────────────────────────────────────────

    @Test
    void reject_aguardandoOrder_doesNotChangeStatus() {
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        orderService.reject(order.getId(), entregadorUser);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.AGUARDANDO_ACEITACAO);
    }

    @Test
    void reject_alreadyConfirmed_throwsBusinessException() {
        order.setStatus(OrderStatus.CONFIRMADO);
        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.reject(order.getId(), entregadorUser))
                .isInstanceOf(BusinessException.class);
    }
}
