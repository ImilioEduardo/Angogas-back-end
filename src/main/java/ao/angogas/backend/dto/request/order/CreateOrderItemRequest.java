package ao.angogas.backend.dto.request.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateOrderItemRequest {

    @NotNull(message = "Produto é obrigatório")
    private UUID productId;

    @Min(value = 1, message = "Quantidade deve ser pelo menos 1")
    private int quantidade = 1;
}
