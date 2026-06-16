package ao.angogas.backend.repository;

import ao.angogas.backend.model.User;
import ao.angogas.backend.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelefone(String telefone);
    Optional<User> findByEmailOrTelefone(String email, String telefone);
    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
    Page<User> findByRole(UserRole role, Pageable pageable);
}
