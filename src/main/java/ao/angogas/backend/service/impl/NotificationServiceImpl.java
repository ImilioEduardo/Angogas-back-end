package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.notification.NotificationResponse;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.exception.UnauthorizedException;
import ao.angogas.backend.model.Notification;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.NotificationType;
import ao.angogas.backend.repository.NotificationRepository;
import ao.angogas.backend.repository.UserRepository;
import ao.angogas.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public PageResponse<NotificationResponse> listMy(User user, Pageable pageable) {
        return PageResponse.from(
                notificationRepository.findByUserIdOrderByCriadoEmDesc(user.getId(), pageable)
                        .map(this::toResponse));
    }

    @Override
    public long countUnread(User user) {
        return notificationRepository.countByUserIdAndLidaFalse(user.getId());
    }

    @Override
    @Transactional
    public void markRead(UUID id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Acesso negado");
        }
        notification.setLida(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(User user) {
        notificationRepository.findByUserIdOrderByCriadoEmDesc(user.getId(), Pageable.unpaged())
                .forEach(n -> {
                    n.setLida(true);
                    notificationRepository.save(n);
                });
    }

    @Override
    @Transactional
    public void send(UUID userId, String titulo, String mensagem, NotificationType tipo, UUID entityId, String route) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado"));

        Notification notification = Notification.builder()
                .user(user)
                .titulo(titulo)
                .mensagem(mensagem)
                .tipo(tipo)
                .entityId(entityId)
                .route(route)
                .build();
        notificationRepository.save(notification);

        log.info("FCM: enviando push para userId={} — {}", userId, titulo);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .mensagem(n.getMensagem())
                .tipo(n.getTipo() != null ? n.getTipo().name() : null)
                .lida(n.isLida())
                .entityId(n.getEntityId())
                .route(n.getRoute())
                .criadoEm(n.getCriadoEm())
                .build();
    }
}
