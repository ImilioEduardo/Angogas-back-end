package ao.angogas.backend.repository;

import ao.angogas.backend.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCriadoEmDesc(UUID userId, Pageable pageable);
    long countByUserIdAndLidaFalse(UUID userId);
}
