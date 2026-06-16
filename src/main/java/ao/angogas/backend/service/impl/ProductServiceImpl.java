package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.product.CreateProductRequest;
import ao.angogas.backend.dto.request.product.UpdateProductRequest;
import ao.angogas.backend.dto.response.PageResponse;
import ao.angogas.backend.dto.response.product.ProductResponse;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.mapper.ProductMapper;
import ao.angogas.backend.model.Product;
import ao.angogas.backend.repository.ProductRepository;
import ao.angogas.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
    }

    @Override
    public PageResponse<ProductResponse> listActive(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return PageResponse.from(
                    productRepository.findByNomeContainingIgnoreCaseAndActivoTrue(search, pageable)
                            .map(productMapper::toResponse));
        }
        return PageResponse.from(
                productRepository.findByActivoTrue(pageable).map(productMapper::toResponse));
    }

    @Override
    public PageResponse<ProductResponse> listAll(Pageable pageable) {
        return PageResponse.from(productRepository.findAll(pageable).map(productMapper::toResponse));
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        productMapper.updateEntity(request, product);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado"));
        product.setActivo(false);
        productRepository.save(product);
    }
}
