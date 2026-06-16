package ao.angogas.backend.dto.response.delivery;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderTrackingResponse {
    private UUID orderId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private OffsetDateTime registadoEm;
}
