package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_agents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 50)
    private String veiculo;

    @Column(length = 20)
    private String matricula;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id")
    private Agency agency;

    // Documentação pessoal
    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "bi_numero", length = 30)
    private String biNumero;

    @Column(name = "carta_conducao", length = 30)
    private String cartaConducao;

    @Column(name = "carta_conducao_desde")
    private LocalDate cartaConducaoDesde;

    @Column(name = "registo_criminal", nullable = false)
    @Builder.Default
    private boolean registoCriminal = false;

    // Documentação do veículo
    @Column(name = "livrete_veiculo", length = 30)
    private String livreteVeiculo;

    @Column(name = "seguro_apolice", length = 50)
    private String seguroApolice;

    @Column(name = "inspecao_validade")
    private LocalDate inspecaoValidade;

    // Equipamento
    @Column(name = "tem_smartphone", nullable = false)
    @Builder.Default
    private boolean temSmartphone = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean disponivel = false;

    @Column(name = "avaliacao_media", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal avaliacaoMedia = BigDecimal.ZERO;

    @Column(name = "total_entregas", nullable = false)
    @Builder.Default
    private int totalEntregas = 0;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;
}
