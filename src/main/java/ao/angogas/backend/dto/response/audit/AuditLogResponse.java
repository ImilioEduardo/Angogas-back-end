package ao.angogas.backend.dto.response.audit;

import java.time.OffsetDateTime;

public record AuditLogResponse(
        Long id,
        String userEmail,
        String accao,
        String entidade,
        String entidadeId,
        String detalhes,
        String ip,
        OffsetDateTime criadoEm
) {}
