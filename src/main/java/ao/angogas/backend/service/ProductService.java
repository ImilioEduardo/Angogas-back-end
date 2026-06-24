package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.product.CreateProductRequest;
import ao.angogas.backend.dto.request.product.UpdateProductRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.product.ProductResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);
    ProductResponse getById(UUID id);
    PageResponse<ProductResponse> listActive(String search, Pageable pageable);
    PageResponse<ProductResponse> listAll(Pageable pageable);
    ProductResponse update(UUID id, UpdateProductRequest request);
    void delete(UUID id);
    void permanentDelete(UUID id);
}
