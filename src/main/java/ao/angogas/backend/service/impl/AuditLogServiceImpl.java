package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.audit.AuditLogResponse;
import ao.angogas.backend.model.AuditLog;
import ao.angogas.backend.repository.AuditLogRepository;
import ao.angogas.backend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async
    @Transactional
    public void log(String userEmail, String accao, String entidade, String entidadeId, String detalhes, String ip) {
        try {
            AuditLog entry = AuditLog.builder()
                    .userEmail(userEmail)
                    .accao(accao)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .detalhes(detalhes)
                    .ip(ip)
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Erro ao guardar audit log: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAll(Pageable pageable) {
        return PageResponse.from(auditLogRepository.findAll(pageable)
                .map(a -> new AuditLogResponse(
                        a.getId(), a.getUserEmail(), a.getAccao(),
                        a.getEntidade(), a.getEntidadeId(), a.getDetalhes(),
                        a.getIp(), a.getCriadoEm())));
    }
}
