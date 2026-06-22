package ao.angogas.backend.repository;

import ao.angogas.backend.model.ProductPricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPricingRepository extends JpaRepository<ProductPricing, UUID> {

    Optional<ProductPricing> findByProductId(UUID productId);

    @Query("SELECT pp FROM ProductPricing pp JOIN FETCH pp.product WHERE pp.activo = true ORDER BY pp.product.nome")
    List<ProductPricing> findAllActiveWithProduct();
}
