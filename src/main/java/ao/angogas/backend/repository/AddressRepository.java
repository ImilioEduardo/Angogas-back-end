package ao.angogas.backend.repository;

import ao.angogas.backend.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserIdOrderByPredefinidoDesc(UUID userId);
    Optional<Address> findByIdAndUserId(UUID id, UUID userId);
}
