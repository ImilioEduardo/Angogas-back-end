package ao.angogas.backend.repository;

import ao.angogas.backend.model.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByTokenAndUsadoFalse(String token);

    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :userId")
    void deleteByUserId(UUID userId);
}
