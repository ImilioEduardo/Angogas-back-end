package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_pricing")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    /** Preço de custo SonaGas (troca de garrafa vazia por cheia). */
    @Column(name = "custo_sonagaz_kz", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoSonagazKz;

    /** Margem de venda aplicada sobre o custo: 0.20 = 20 %. */
    @Column(name = "margem_produto", nullable = false, precision = 5, scale = 4)
    private BigDecimal margemProduto;

    /** Depósito cobrado a clientes sem garrafa vazia para troca. */
    @Column(name = "deposito_garrafa_kz", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositoGarrafaKz;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "actualizado_em")
    private OffsetDateTime actualizadoEm;
}
