package ao.angogas.backend.repository;

import ao.angogas.backend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByOrderId(UUID orderId);
    Page<Review> findByAgentId(UUID agentId, Pageable pageable);

    @Query("SELECT AVG(r.nota) FROM Review r WHERE r.agent.id = :agentId")
    Double calcularMediaByAgentId(@Param("agentId") UUID agentId);
}
