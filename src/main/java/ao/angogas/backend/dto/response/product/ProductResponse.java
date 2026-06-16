package ao.angogas.backend.dto.response.product;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {
    private UUID id;
    private String nome;
    private String descricao;
    private BigDecimal precoKz;
    private BigDecimal pesoKg;
    private int stock;
    private String imagemUrl;
    private boolean activo;
    private OffsetDateTime criadoEm;
}
