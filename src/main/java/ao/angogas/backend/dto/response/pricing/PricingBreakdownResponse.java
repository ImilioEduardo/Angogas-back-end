package ao.angogas.backend.dto.response.pricing;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PricingBreakdownResponse {

    /** Produto × quantidade (preço de venda) + depósito de garrafa se aplicável */
    private BigDecimal subtotalProduto;

    /** Taxa base = custo fixo por entrega × (1 + margemEntrega) */
    private BigDecimal taxaBase;

    /** precoPorKm × max(distancia, distanciaMin) */
    private BigDecimal taxaDistancia;

    /** precoPorMinuto × tempoEstimadoMin */
    private BigDecimal taxaTempo;

    /** Depósito de garrafa (0 se cliente tem garrafa) */
    private BigDecimal depositoGarrafa;

    /** subtotalProduto + taxaBase + taxaDistancia + taxaTempo */
    private BigDecimal subtotal;

    /** subtotal × comissaoApp */
    private BigDecimal comissao;

    /** subtotal + comissao */
    private BigDecimal total;

    /** Distância calculada (km) */
    private BigDecimal distanciaKm;

    /** Tempo estimado (minutos) */
    private int tempoEstimadoMin;
}
