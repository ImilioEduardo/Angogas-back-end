package ao.angogas.backend.dto.request.zone;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateZoneRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Município é obrigatório")
    private String municipio;
}
