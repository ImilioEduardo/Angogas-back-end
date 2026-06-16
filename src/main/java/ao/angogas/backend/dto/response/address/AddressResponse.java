package ao.angogas.backend.dto.response.address;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class AddressResponse {
    private UUID id;
    private String rua;
    private String bairro;
    private String municipio;
    private String referencia;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean predefinido;
}
