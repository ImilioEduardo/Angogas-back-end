package ao.angogas.backend.repository;

import ao.angogas.backend.model.Payment;
import ao.angogas.backend.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(UUID orderId);
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
