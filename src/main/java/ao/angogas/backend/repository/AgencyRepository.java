package ao.angogas.backend.repository;

import ao.angogas.backend.model.Agency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgencyRepository extends JpaRepository<Agency, UUID> {
    List<Agency> findAllByOrderByNomeAsc();
    boolean existsByNif(String nif);
}
