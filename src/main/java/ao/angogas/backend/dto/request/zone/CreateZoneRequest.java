package ao.angogas.backend.dto.request.zone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateZoneRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Município é obrigatório")
    private String municipio;

    @NotEmpty(message = "Coordenadas do polígono são obrigatórias")
    private List<CoordenadasPonto> coordenadas;

    @Data
    public static class CoordenadasPonto {
        private double lat;
        private double lng;
    }
}
