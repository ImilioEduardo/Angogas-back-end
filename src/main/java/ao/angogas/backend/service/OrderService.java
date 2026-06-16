package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.order.CreateOrderRequest;
import ao.angogas.backend.dto.request.order.UpdateOrderStatusRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.model.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponse create(CreateOrderRequest request, User currentUser);
    OrderResponse getById(UUID id, User currentUser);
    PageResponse<OrderResponse> listByClient(User currentUser, Pageable pageable);
    PageResponse<OrderResponse> listAll(String status, Pageable pageable);
    OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request);
    void cancel(UUID id, User currentUser);
}
