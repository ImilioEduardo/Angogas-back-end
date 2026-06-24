package ao.angogas.backend.dto.response.order;

import ao.angogas.backend.dto.response.address.AddressResponse;
import ao.angogas.backend.model.enums.OrderStatus;
import ao.angogas.backend.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderResponse {
    private UUID id;
    private UUID clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private UUID entregadorId;
    private String entregadorNome;
    private String entregadorTelefone;
    private String entregadorFotoPerfil;
    private AddressResponse address;
    private UUID zoneId;
    private String zoneNome;
    private OrderStatus status;
    private PaymentMethod metodoPagamento;
    private BigDecimal totalKz;
    private BigDecimal descontoPontos;
    private String codigoEntrega;
    private String notas;
    private List<OrderItemResponse> items;
    private OffsetDateTime criadoEm;
    private OffsetDateTime actualizadoEm;
}
