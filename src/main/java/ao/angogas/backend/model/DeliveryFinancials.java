package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_financials")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryFinancials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    /** Distância real percorrida pelo estafeta (km). */
    @Column(name = "distancia_real_km", nullable = false, precision = 8, scale = 3)
    @Builder.Default
    private BigDecimal distanciaRealKm = BigDecimal.ZERO;

    /** Tempo real da entrega (minutos). */
    @Column(name = "tempo_real_min", nullable = false)
    @Builder.Default
    private int tempoRealMin = 0;

    /** Valor total cobrado ao cliente. */
    @Column(name = "preco_cobrado_kz", nullable = false, precision = 14, scale = 2)
    private BigDecimal precoCobradoKz;

    /** Custo SonaGas dos produtos entregues. */
    @Column(name = "custo_produto_kz", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal custoProdutoKz = BigDecimal.ZERO;

    /** Taxa de entrega efectivamente cobrada (taxaBase + distância + tempo). */
    @Column(name = "taxa_entrega_kz", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal taxaEntregaKz = BigDecimal.ZERO;

    /** Comissão retida pela plataforma (precoCobrado × comissaoApp). */
    @Column(name = "comissao_plataforma_kz", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal comissaoPlataformaKz = BigDecimal.ZERO;

    /**
     * Lucro líquido estimado:
     * precoCobrado - custoProduto - custoFixoPorEntrega
     */
    @Column(name = "lucro_liquido_kz", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal lucroLiquidoKz = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;
}
