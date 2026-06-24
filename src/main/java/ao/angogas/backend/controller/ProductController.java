package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.product.CreateProductRequest;
import ao.angogas.backend.dto.request.product.UpdateProductRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Catálogo de botijões de gás")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar produtos activos")
    public ResponseEntity<ApiResponse<?>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        return ResponseEntity.ok(ApiResponse.ok(productService.listActive(search, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver produto por ID")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Criar produto")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(productService.create(request), "Produto criado com sucesso"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Actualizar produto")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Desactivar produto (soft delete)")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Produto desactivado"));
    }

    @DeleteMapping("/{id}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Apagar produto permanentemente")
    public ResponseEntity<ApiResponse<?>> permanentDelete(@PathVariable UUID id) {
        productService.permanentDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Produto apagado permanentemente"));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todos os produtos (incluindo inactivos)")
    public ResponseEntity<ApiResponse<?>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("criadoEm").descending());
        return ResponseEntity.ok(ApiResponse.ok(productService.listAll(pageable)));
    }
}
