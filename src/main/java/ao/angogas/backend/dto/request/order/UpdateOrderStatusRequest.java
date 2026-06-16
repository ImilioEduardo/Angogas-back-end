package ao.angogas.backend.dto.request.order;

import ao.angogas.backend.model.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private OrderStatus status;

    private UUID entregadorId;
}
