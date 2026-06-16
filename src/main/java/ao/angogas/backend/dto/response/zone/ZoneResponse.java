package ao.angogas.backend.dto.response.zone;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ZoneResponse {
    private UUID id;
    private String nome;
    private String municipio;
    private boolean activa;
}
