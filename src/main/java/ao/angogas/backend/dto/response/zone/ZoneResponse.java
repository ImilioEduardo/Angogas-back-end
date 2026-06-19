package ao.angogas.backend.dto.response.zone;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ZoneResponse {
    private UUID id;
    private String nome;
    private String municipio;
    private boolean activa;
    private List<CoordenadasPonto> coordenadas;

    @Data
    @Builder
    public static class CoordenadasPonto {
        private double lat;
        private double lng;
    }
}
