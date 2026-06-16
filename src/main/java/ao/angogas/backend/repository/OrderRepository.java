package ao.angogas.backend.repository;

import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByClienteId(UUID clienteId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByEntregadorId(UUID entregadorId, Pageable pageable);
    Page<Order> findByEntregadorIdAndStatusNotIn(UUID entregadorId, Collection<OrderStatus> statuses, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.cliente LEFT JOIN FETCH o.entregador LEFT JOIN FETCH o.address LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.product WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.cliente LEFT JOIN FETCH o.entregador LEFT JOIN FETCH o.address WHERE o.id IN :ids")
    List<Order> findAllWithAssociationsByIdIn(@Param("ids") Collection<UUID> ids);

    @Query("SELECT o.id FROM Order o WHERE o.cliente.id = :clienteId")
    Page<UUID> findIdsByClienteId(@Param("clienteId") UUID clienteId, Pageable pageable);

    @Query("SELECT o.id FROM Order o")
    Page<UUID> findAllIds(Pageable pageable);

    @Query("SELECT o.id FROM Order o WHERE o.status = :status")
    Page<UUID> findIdsByStatus(@Param("status") OrderStatus status, Pageable pageable);
}
