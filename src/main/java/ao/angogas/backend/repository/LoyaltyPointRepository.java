package ao.angogas.backend.repository;

import ao.angogas.backend.model.LoyaltyPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LoyaltyPointRepository extends JpaRepository<LoyaltyPoint, UUID> {
    Page<LoyaltyPoint> findByUserId(UUID userId, Pageable pageable);

    @Query("SELECT SUM(lp.pontos) FROM LoyaltyPoint lp WHERE lp.user.id = :userId")
    Integer sumPontosByUserId(@Param("userId") UUID userId);
}
