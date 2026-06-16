package ao.angogas.backend.dto.request.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAddressRequest {

    @Size(max = 200)
    private String rua;

    @NotBlank(message = "Bairro é obrigatório")
    @Size(max = 100)
    private String bairro;

    @NotBlank(message = "Município é obrigatório")
    @Size(max = 100)
    private String municipio;

    private String referencia;

    private BigDecimal latitude;
    private BigDecimal longitude;

    private boolean predefinido = false;
}
