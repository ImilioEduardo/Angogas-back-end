package ao.angogas.backend.dto.response.review;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID orderId;
    private String nomeCliente;
    private String nomeAgent;
    private short nota;
    private String comentario;
    private OffsetDateTime criadoEm;
}
