package ao.angogas.backend.mapper;

import ao.angogas.backend.dto.request.product.CreateProductRequest;
import ao.angogas.backend.dto.response.product.ProductResponse;
import ao.angogas.backend.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ao.angogas.backend.dto.request.product.UpdateProductRequest;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {
    Product toEntity(CreateProductRequest request);
    ProductResponse toResponse(Product product);
    void updateEntity(UpdateProductRequest request, @MappingTarget Product product);
}
