package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.delivery.AssignZoneRequest;
import ao.angogas.backend.dto.request.delivery.CreateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryStatusRequest;
import ao.angogas.backend.dto.request.delivery.UpdateLocationRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.delivery.DeliveryAgentResponse;
import ao.angogas.backend.dto.response.delivery.OrderTrackingResponse;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.mapper.OrderMapper;
import ao.angogas.backend.model.DeliveryAgent;
import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.OrderTracking;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.Zone;
import ao.angogas.backend.model.enums.NotificationType;
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.UserRole;
import ao.angogas.backend.repository.DeliveryAgentRepository;
import ao.angogas.backend.repository.OrderRepository;
import ao.angogas.backend.repository.OrderTrackingRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.repository.ZoneRepository;
import ao.angogas.backend.service.DeliveryAgentService;
import ao.angogas.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryAgentServiceImpl implements DeliveryAgentService {

    private static final List<OrderStatus> ENTREGADOR_STATUS_FLOW = List.of(
            OrderStatus.CONFIRMADO,
            OrderStatus.A_PREPARAR,
            OrderStatus.A_CAMINHO,
            OrderStatus.ENTREGUE
    );

    private final DeliveryAgentRepository deliveryAgentRepository;
    private final OrderRepository orderRepository;
    private final OrderTrackingRepository trackingRepository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listMyOrders(User user, Pageable pageable) {
        Page<Order> page = orderRepository.findByEntregadorIdAndStatusNotIn(
                user.getId(),
                List.of(OrderStatus.CANCELADO),
                pageable
        );
        return PageResponse.from(page.map(orderMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listAvailableOrders(User user, Pageable pageable) {
        DeliveryAgent agent = deliveryAgentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de entregador não encontrado"));

        if (agent.getZone() == null) {
            return PageResponse.from(org.springframework.data.domain.Page.empty(pageable));
        }

        Page<UUID> idPage = orderRepository.findIdsByStatusAndZoneId(
                OrderStatus.AGUARDANDO_ACEITACAO, agent.getZone().getId(), pageable);
        List<Order> orders = orderRepository.findAllWithAssociationsByIdIn(idPage.getContent());

        return PageResponse.from(new org.springframework.data.domain.PageImpl<>(
                orders.stream().map(orderMapper::toResponse).toList(),
                pageable,
                idPage.getTotalElements()));
    }

    @Override
    @Transactional
    public OrderResponse updateDeliveryStatus(UUID orderId, UpdateDeliveryStatusRequest request, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getEntregador() == null || !order.getEntregador().getId().equals(user.getId())) {
            throw new UnauthorizedException("Este pedido não está atribuído a ti");
        }

        int currentIndex = ENTREGADOR_STATUS_FLOW.indexOf(order.getStatus());
        int nextIndex = ENTREGADOR_STATUS_FLOW.indexOf(request.getStatus());

        if (nextIndex == -1 || nextIndex != currentIndex + 1) {
            throw new BusinessException("Transição de status inválida: " + order.getStatus() + " → " + request.getStatus());
        }

        order.setStatus(request.getStatus());
        Order saved = orderRepository.save(order);

        notificationService.send(
                order.getCliente().getId(),
                "Actualização do teu pedido",
                "O teu pedido está agora: " + request.getStatus().name(),
                NotificationType.ENTREGA
        );

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderTrackingResponse updateLocation(UUID orderId, UpdateLocationRequest request, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));

        if (order.getEntregador() == null || !order.getEntregador().getId().equals(user.getId())) {
            throw new UnauthorizedException("Este pedido não está atribuído a ti");
        }

        OrderTracking tracking = OrderTracking.builder()
                .order(order)
                .agent(user)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();
        trackingRepository.save(tracking);

        OrderTrackingResponse response = OrderTrackingResponse.builder()
                .orderId(orderId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .registadoEm(tracking.getRegistadoEm())
                .build();

        messagingTemplate.convertAndSend("/topic/tracking/" + orderId, response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryAgentResponse getMyProfile(User user) {
        DeliveryAgent agent = deliveryAgentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de entregador não encontrado"));
        return toResponse(agent);
    }

    @Override
    @Transactional
    public DeliveryAgentResponse toggleDisponivel(User user) {
        DeliveryAgent agent = deliveryAgentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de entregador não encontrado"));
        agent.setDisponivel(!agent.isDisponivel());
        return toResponse(deliveryAgentRepository.save(agent));
    }

    @Override
    @Transactional
    public DeliveryAgentResponse createAgent(CreateDeliveryAgentRequest request) {
        if (request.getEmail() == null && request.getTelefone() == null) {
            throw new BusinessException("Email ou telefone é obrigatório");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já registado");
        }
        if (request.getTelefone() != null && userRepository.existsByTelefone(request.getTelefone())) {
            throw new BusinessException("Telefone já registado");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.ENTREGADOR)
                .build();
        user = userRepository.save(user);

        DeliveryAgent agent = DeliveryAgent.builder()
                .user(user)
                .dataNascimento(request.getDataNascimento())
                .biNumero(request.getBiNumero())
                .cartaConducao(request.getCartaConducao())
                .cartaConducaoDesde(request.getCartaConducaoDesde())
                .registoCriminal(request.isRegistoCriminal())
                .veiculo(request.getVeiculo())
                .matricula(request.getMatricula())
                .livreteVeiculo(request.getLivreteVeiculo())
                .seguroApolice(request.getSeguroApolice())
                .inspecaoValidade(request.getInspecaoValidade())
                .temSmartphone(request.isTemSmartphone())
                .build();
        return toResponse(deliveryAgentRepository.save(agent));
    }

    @Override
    @Transactional
    public DeliveryAgentResponse updateAgent(UUID agentId, UpdateDeliveryAgentRequest request) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));

        User user = agent.getUser();

        if (request.getNome() != null && !request.getNome().isBlank()) {
            user.setNome(request.getNome());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("Email já registado por outro utilizador");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getTelefone() != null && !request.getTelefone().isBlank()) {
            if (!request.getTelefone().equals(user.getTelefone()) && userRepository.existsByTelefone(request.getTelefone())) {
                throw new BusinessException("Telefone já registado por outro utilizador");
            }
            user.setTelefone(request.getTelefone());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActivo() != null) {
            user.setActivo(request.getActivo());
        }
        userRepository.save(user);

        if (request.getDataNascimento() != null) agent.setDataNascimento(request.getDataNascimento());
        if (request.getBiNumero() != null && !request.getBiNumero().isBlank()) agent.setBiNumero(request.getBiNumero());
        if (request.getCartaConducao() != null && !request.getCartaConducao().isBlank()) agent.setCartaConducao(request.getCartaConducao());
        if (request.getCartaConducaoDesde() != null) agent.setCartaConducaoDesde(request.getCartaConducaoDesde());
        if (request.getRegistoCriminal() != null) agent.setRegistoCriminal(request.getRegistoCriminal());
        if (request.getVeiculo() != null) agent.setVeiculo(request.getVeiculo().isBlank() ? null : request.getVeiculo());
        if (request.getMatricula() != null) agent.setMatricula(request.getMatricula().isBlank() ? null : request.getMatricula());
        if (request.getLivreteVeiculo() != null) agent.setLivreteVeiculo(request.getLivreteVeiculo().isBlank() ? null : request.getLivreteVeiculo());
        if (request.getSeguroApolice() != null) agent.setSeguroApolice(request.getSeguroApolice().isBlank() ? null : request.getSeguroApolice());
        if (request.getInspecaoValidade() != null) agent.setInspecaoValidade(request.getInspecaoValidade());
        if (request.getTemSmartphone() != null) agent.setTemSmartphone(request.getTemSmartphone());

        return toResponse(deliveryAgentRepository.save(agent));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeliveryAgentResponse> listAll(Pageable pageable) {
        return PageResponse.from(deliveryAgentRepository.findAll(pageable).map(this::toResponse));
    }

    @Override
    @Transactional
    public DeliveryAgentResponse assignZone(UUID agentId, AssignZoneRequest request) {
        DeliveryAgent agent = deliveryAgentRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Entregador não encontrado"));
        Zone zone = zoneRepository.findById(request.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zona não encontrada"));
        agent.setZone(zone);
        return toResponse(deliveryAgentRepository.save(agent));
    }

    @Override
    public OrderTrackingResponse getLastLocation(UUID orderId) {
        return trackingRepository.findTopByOrderIdOrderByRegistadoEmDesc(orderId)
                .map(t -> OrderTrackingResponse.builder()
                        .orderId(orderId)
                        .latitude(t.getLatitude())
                        .longitude(t.getLongitude())
                        .registadoEm(t.getRegistadoEm())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Localização não disponível para este pedido"));
    }

    private DeliveryAgentResponse toResponse(DeliveryAgent agent) {
        return DeliveryAgentResponse.builder()
                .id(agent.getId())
                .userId(agent.getUser().getId())
                .nome(agent.getUser().getNome())
                .email(agent.getUser().getEmail())
                .telefone(agent.getUser().getTelefone())
                .dataNascimento(agent.getDataNascimento())
                .biNumero(agent.getBiNumero())
                .cartaConducao(agent.getCartaConducao())
                .cartaConducaoDesde(agent.getCartaConducaoDesde())
                .registoCriminal(agent.isRegistoCriminal())
                .veiculo(agent.getVeiculo())
                .matricula(agent.getMatricula())
                .livreteVeiculo(agent.getLivreteVeiculo())
                .seguroApolice(agent.getSeguroApolice())
                .inspecaoValidade(agent.getInspecaoValidade())
                .temSmartphone(agent.isTemSmartphone())
                .zoneId(agent.getZone() != null ? agent.getZone().getId() : null)
                .zoneName(agent.getZone() != null ? agent.getZone().getNome() : null)
                .disponivel(agent.isDisponivel())
                .activo(agent.getUser().isActivo())
                .avaliacaoMedia(agent.getAvaliacaoMedia())
                .totalEntregas(agent.getTotalEntregas())
                .criadoEm(agent.getCriadoEm())
                .build();
    }
}
