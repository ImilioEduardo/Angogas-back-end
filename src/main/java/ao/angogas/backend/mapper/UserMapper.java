package ao.angogas.backend.mapper;

import ao.angogas.backend.dto.response.user.UserResponse;
import ao.angogas.backend.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
