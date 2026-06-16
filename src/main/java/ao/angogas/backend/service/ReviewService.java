package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.review.CreateReviewRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.review.ReviewResponse;
import ao.angogas.backend.model.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse create(CreateReviewRequest request, User currentUser);
    PageResponse<ReviewResponse> listByAgent(UUID agentId, Pageable pageable);
}
