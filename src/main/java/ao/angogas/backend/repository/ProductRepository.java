package ao.angogas.backend.repository;

import ao.angogas.backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByActivoTrue(Pageable pageable);
    Page<Product> findByNomeContainingIgnoreCaseAndActivoTrue(String nome, Pageable pageable);
    Page<Product> findAll(Pageable pageable);
}
