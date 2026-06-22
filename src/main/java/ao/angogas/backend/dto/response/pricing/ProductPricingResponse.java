package ao.angogas.backend.dto.response.pricing;

import ao.angogas.backend.model.ProductPricing;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Builder
public class ProductPricingResponse {

    private UUID id;
    private UUID produtoId;
    private String produtoNome;
    private BigDecimal custoSonagazKz;
    private BigDecimal margemProduto;
    private BigDecimal depositoGarrafaKz;
    /** precoVenda = custoSonagaz × (1 + margem) — referência, não necessariamente igual a products.preco_kz */
    private BigDecimal precoVendaCalculado;
    private boolean activo;

    public static ProductPricingResponse from(ProductPricing pp) {
        BigDecimal precoVenda = pp.getCustoSonagazKz()
                .multiply(BigDecimal.ONE.add(pp.getMargemProduto()))
                .setScale(2, RoundingMode.HALF_UP);
        return ProductPricingResponse.builder()
                .id(pp.getId())
                .produtoId(pp.getProduct().getId())
                .produtoNome(pp.getProduct().getNome())
                .custoSonagazKz(pp.getCustoSonagazKz())
                .margemProduto(pp.getMargemProduto())
                .depositoGarrafaKz(pp.getDepositoGarrafaKz())
                .precoVendaCalculado(precoVenda)
                .activo(pp.isActivo())
                .build();
    }
}
