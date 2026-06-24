package ao.angogas.backend.dto.response.notification;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String titulo;
    private String mensagem;
    private String tipo;
    private boolean lida;
    private UUID entityId;
    private String route;
    private OffsetDateTime criadoEm;
}
