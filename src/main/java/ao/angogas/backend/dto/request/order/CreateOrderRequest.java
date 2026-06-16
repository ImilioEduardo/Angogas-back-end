package ao.angogas.backend.dto.request.order;

import ao.angogas.backend.model.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateOrderRequest {

    @NotNull(message = "Endereço de entrega é obrigatório")
    private UUID addressId;

    @NotNull(message = "Método de pagamento é obrigatório")
    private PaymentMethod metodoPagamento;

    @NotEmpty(message = "O pedido deve ter pelo menos um produto")
    @Valid
    private List<CreateOrderItemRequest> items;

    private String notas;
}
