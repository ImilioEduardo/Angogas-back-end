package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.user.UpdateUserRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.user.UserResponse;
import ao.angogas.backend.model.User;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse getMe(User currentUser);
    UserResponse update(UUID id, UpdateUserRequest request);
    PageResponse<UserResponse> listAll(Pageable pageable);
    UserResponse getById(UUID id);
    void deactivate(UUID id);
}
