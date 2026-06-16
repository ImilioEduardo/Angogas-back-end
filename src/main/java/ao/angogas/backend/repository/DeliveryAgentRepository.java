package ao.angogas.backend.repository;

import ao.angogas.backend.model.DeliveryAgent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, UUID> {
    Optional<DeliveryAgent> findByUserId(UUID userId);
    List<DeliveryAgent> findByZoneIdAndDisponivelTrue(UUID zoneId);
    List<DeliveryAgent> findByDisponivelTrue();
    Page<DeliveryAgent> findAll(Pageable pageable);
}
