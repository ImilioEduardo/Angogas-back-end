package ao.angogas.backend.dto.response.agency;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgencyResponse {
    private UUID id;
    private String nome;
    private String nif;
    private String responsavel;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String morada;
    private boolean activa;
    private OffsetDateTime criadoEm;
}
