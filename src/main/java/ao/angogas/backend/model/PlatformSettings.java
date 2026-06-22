package ao.angogas.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettings {

    /** Singleton: a tabela tem sempre exactamente uma linha com id = 1. */
    @Id
    private Long id;

    @Column(name = "preco_por_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPorKm;

    @Column(name = "preco_por_minuto", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoPorMinuto;

    /** Percentagem como decimal: 0.10 = 10 % */
    @Column(name = "comissao_app", nullable = false, precision = 5, scale = 4)
    private BigDecimal comissaoApp;

    @Column(name = "distancia_min_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal distanciaMinKm;

    @Column(name = "distancia_max_km", nullable = false, precision = 5, scale = 2)
    private BigDecimal distanciaMaxKm;

    /** Margem aplicada sobre o custo fixo por entrega: 0.25 = 25 % */
    @Column(name = "margem_entrega", nullable = false, precision = 5, scale = 4)
    private BigDecimal margemEntrega;

    @Column(name = "quantidade_max_botijas", nullable = false)
    private int quantidadeMaxBotijas;

    @Column(name = "custofixo_mensal", nullable = false, precision = 14, scale = 2)
    private BigDecimal custofixoMensal;

    @Column(name = "entregas_estimadas_mes", nullable = false)
    private int entregasEstimadasMes;

    /** Coordenadas do armazém para cálculo de distância (Haversine). */
    @Column(name = "armazem_latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal armazemLatitude;

    @Column(name = "armazem_longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal armazemLongitude;

    /** Velocidade média de entrega em Luanda (km/h) — usada para estimar o tempo. */
    @Column(name = "velocidade_media_kmh", nullable = false, precision = 5, scale = 2)
    private BigDecimal velocidadeMediaKmh;
}
