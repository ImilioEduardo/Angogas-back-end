package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.subscription.CreateSubscriptionRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.subscription.SubscriptionResponse;
import ao.angogas.backend.model.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SubscriptionService {
    SubscriptionResponse create(CreateSubscriptionRequest request, User user);
    SubscriptionResponse getMy(User user);
    void cancel(UUID id, User user);
    PageResponse<SubscriptionResponse> listAll(Pageable pageable);
}
