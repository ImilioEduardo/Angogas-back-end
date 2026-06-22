package ao.angogas.backend.dto.request.agency;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateAgencyRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome não pode exceder 150 caracteres")
    private String nome;

    @NotBlank(message = "NIF é obrigatório")
    @Size(max = 20, message = "NIF não pode exceder 20 caracteres")
    private String nif;

    @NotBlank(message = "Responsável é obrigatório")
    @Size(max = 100, message = "Nome do responsável não pode exceder 100 caracteres")
    private String responsavel;

    @DecimalMin(value = "-90.0", message = "Latitude inválida")
    @DecimalMax(value = "90.0",  message = "Latitude inválida")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude inválida")
    @DecimalMax(value = "180.0",  message = "Longitude inválida")
    private BigDecimal longitude;

    @Size(max = 255, message = "Morada não pode exceder 255 caracteres")
    private String morada;
}
