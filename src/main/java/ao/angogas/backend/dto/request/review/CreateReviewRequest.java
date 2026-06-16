package ao.angogas.backend.dto.request.review;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {
    @NotNull(message = "ID do pedido é obrigatório")
    private UUID orderId;

    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota mínima é 1")
    @Max(value = 5, message = "Nota máxima é 5")
    private Short nota;

    private String comentario;
}
