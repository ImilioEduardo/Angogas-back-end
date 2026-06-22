package ao.angogas.backend.dto.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignAgencyRequest {

    @NotNull(message = "ID da agência é obrigatório")
    private UUID agencyId;
}
