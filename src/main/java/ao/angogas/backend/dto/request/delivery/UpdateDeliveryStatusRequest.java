package ao.angogas.backend.dto.request.delivery;

import ao.angogas.backend.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDeliveryStatusRequest {
    @NotNull(message = "Status é obrigatório")
    private OrderStatus status;
}
