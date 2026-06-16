package ao.angogas.backend.service;

import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.notification.NotificationResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    PageResponse<NotificationResponse> listMy(User user, Pageable pageable);
    long countUnread(User user);
    void markRead(UUID id, User user);
    void markAllRead(User user);
    void send(UUID userId, String titulo, String mensagem, NotificationType tipo);
}
