package ao.angogas.backend.repository;

import ao.angogas.backend.model.Subscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByUserId(UUID userId);
    Optional<Subscription> findByUserIdAndActivaTrue(UUID userId);
    Page<Subscription> findAll(Pageable pageable);
}
