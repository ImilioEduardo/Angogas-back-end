package ao.angogas.backend.repository;

import ao.angogas.backend.model.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    Optional<OrderTracking> findTopByOrderIdOrderByRegistadoEmDesc(UUID orderId);
    List<OrderTracking> findByOrderIdOrderByRegistadoEmAsc(UUID orderId);
}
