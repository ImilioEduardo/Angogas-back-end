package ao.angogas.backend.repository;

import ao.angogas.backend.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenAndRevogadoFalse(String token);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.revogado = true WHERE t.user.id = :userId")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
