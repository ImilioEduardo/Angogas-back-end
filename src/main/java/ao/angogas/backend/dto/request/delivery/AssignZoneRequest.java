package ao.angogas.backend.dto.request.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignZoneRequest {
    @NotNull(message = "ID da zona é obrigatório")
    private UUID zoneId;
}
