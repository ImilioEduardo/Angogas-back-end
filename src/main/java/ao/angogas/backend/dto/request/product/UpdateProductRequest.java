package ao.angogas.backend.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateProductRequest {

    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    private String descricao;

    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero")
    private BigDecimal precoKz;

    @DecimalMin(value = "0.1", message = "Peso deve ser maior que zero")
    private BigDecimal pesoKg;

    @Min(value = 0, message = "Stock não pode ser negativo")
    private Integer stock;

    private String imagemUrl;

    private Boolean activo;
}
