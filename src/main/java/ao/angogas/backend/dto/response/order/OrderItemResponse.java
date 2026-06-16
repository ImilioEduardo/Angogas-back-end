package ao.angogas.backend.dto.response.order;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderItemResponse {
    private UUID id;
    private UUID productId;
    private String productNome;
    private String productImagemUrl;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
}
