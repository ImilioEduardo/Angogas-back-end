package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.delivery.AssignAgencyRequest;
import ao.angogas.backend.dto.request.delivery.AssignZoneRequest;
import ao.angogas.backend.dto.request.delivery.CreateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryAgentRequest;
import ao.angogas.backend.dto.request.delivery.UpdateDeliveryStatusRequest;
import ao.angogas.backend.dto.request.delivery.UpdateLocationRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.delivery.DeliveryAgentResponse;
import ao.angogas.backend.dto.response.delivery.OrderTrackingResponse;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.model.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DeliveryAgentService {
    PageResponse<OrderResponse> listMyOrders(User user, Pageable pageable);
    PageResponse<OrderResponse> listAvailableOrders(User user, Pageable pageable);
    OrderResponse updateDeliveryStatus(UUID orderId, UpdateDeliveryStatusRequest request, User user);
    OrderTrackingResponse updateLocation(UUID orderId, UpdateLocationRequest request, User user);
    DeliveryAgentResponse getMyProfile(User user);
    DeliveryAgentResponse toggleDisponivel(User user);
    DeliveryAgentResponse createAgent(CreateDeliveryAgentRequest request);
    DeliveryAgentResponse updateAgent(UUID agentId, UpdateDeliveryAgentRequest request);
    PageResponse<DeliveryAgentResponse> listAll(Pageable pageable);
    DeliveryAgentResponse assignZone(UUID agentId, AssignZoneRequest request);
    DeliveryAgentResponse assignAgency(UUID agentId, AssignAgencyRequest request);
    OrderTrackingResponse getLastLocation(UUID orderId);
    List<OrderTrackingResponse> getTrackingHistory(UUID orderId, User currentUser);
    DeliveryAgentResponse getAgentPublicProfile(UUID userId);
}
