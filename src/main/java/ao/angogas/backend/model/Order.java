package ao.angogas.backend.model;

import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private User cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    private User entregador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pagamento", length = 30)
    private PaymentMethod metodoPagamento;

    @Column(name = "total_kz", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalKz;

    @Column(name = "codigo_entrega", length = 12)
    private String codigoEntrega;

    @Column(name = "desconto_pontos", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descontoPontos = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "actualizado_em")
    private OffsetDateTime actualizadoEm;
}
