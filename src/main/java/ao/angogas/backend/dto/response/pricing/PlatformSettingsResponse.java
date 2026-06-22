package ao.angogas.backend.dto.response.pricing;

import ao.angogas.backend.model.PlatformSettings;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PlatformSettingsResponse {

    private BigDecimal precoPorKm;
    private BigDecimal precoPorMinuto;
    private BigDecimal comissaoApp;
    private BigDecimal distanciaMinKm;
    private BigDecimal distanciaMaxKm;
    private BigDecimal margemEntrega;
    private int quantidadeMaxBotijas;
    private BigDecimal custofixoMensal;
    private int entregasEstimadasMes;
    private BigDecimal armazemLatitude;
    private BigDecimal armazemLongitude;
    private BigDecimal velocidadeMediaKmh;

    /** Custo fixo por entrega calculado = custofixoMensal / entregasEstimadasMes */
    private BigDecimal custofixoPorEntrega;

    public static PlatformSettingsResponse from(PlatformSettings s) {
        BigDecimal cfpe = s.getCustofixoMensal()
                .divide(BigDecimal.valueOf(s.getEntregasEstimadasMes()), 2, java.math.RoundingMode.HALF_UP);
        return PlatformSettingsResponse.builder()
                .precoPorKm(s.getPrecoPorKm())
                .precoPorMinuto(s.getPrecoPorMinuto())
                .comissaoApp(s.getComissaoApp())
                .distanciaMinKm(s.getDistanciaMinKm())
                .distanciaMaxKm(s.getDistanciaMaxKm())
                .margemEntrega(s.getMargemEntrega())
                .quantidadeMaxBotijas(s.getQuantidadeMaxBotijas())
                .custofixoMensal(s.getCustofixoMensal())
                .entregasEstimadasMes(s.getEntregasEstimadasMes())
                .armazemLatitude(s.getArmazemLatitude())
                .armazemLongitude(s.getArmazemLongitude())
                .velocidadeMediaKmh(s.getVelocidadeMediaKmh())
                .custofixoPorEntrega(cfpe)
                .build();
    }
}
