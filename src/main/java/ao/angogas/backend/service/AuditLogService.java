package ao.angogas.backend.service;

import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.audit.AuditLogResponse;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {
    void log(String userEmail, String accao, String entidade, String entidadeId, String detalhes, String ip);
    PageResponse<AuditLogResponse> listAll(Pageable pageable);
}
