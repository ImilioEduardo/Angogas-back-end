package ao.angogas.backend.mapper;

import ao.angogas.backend.dto.response.order.OrderItemResponse;
import ao.angogas.backend.dto.response.order.OrderResponse;
import ao.angogas.backend.model.Order;
import ao.angogas.backend.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface OrderMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNome", source = "cliente.nome")
    @Mapping(target = "entregadorId", source = "entregador.id")
    @Mapping(target = "entregadorNome", source = "entregador.nome")
    @Mapping(target = "address", source = "address")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productNome", source = "product.nome")
    @Mapping(target = "productImagemUrl", source = "product.imagemUrl")
    @Mapping(target = "subtotal", expression = "java(item.getPrecoUnitario().multiply(java.math.BigDecimal.valueOf(item.getQuantidade())))")
    OrderItemResponse toItemResponse(OrderItem item);
}
