package ao.angogas.backend.repository;

import ao.angogas.backend.model.DeliveryFinancials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryFinancialsRepository extends JpaRepository<DeliveryFinancials, UUID> {

    Optional<DeliveryFinancials> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    @Query("SELECT COALESCE(SUM(df.precoCobradoKz), 0) FROM DeliveryFinancials df WHERE df.criadoEm BETWEEN :inicio AND :fim")
    BigDecimal sumReceitaEntreDatas(OffsetDateTime inicio, OffsetDateTime fim);

    @Query("SELECT COALESCE(SUM(df.lucroLiquidoKz), 0) FROM DeliveryFinancials df WHERE df.criadoEm BETWEEN :inicio AND :fim")
    BigDecimal sumLucroEntreDatas(OffsetDateTime inicio, OffsetDateTime fim);

    @Query("SELECT COUNT(df) FROM DeliveryFinancials df WHERE df.criadoEm BETWEEN :inicio AND :fim")
    long countEntreDatas(OffsetDateTime inicio, OffsetDateTime fim);
}
