package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.address.CreateAddressRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.model.User;
import ao.angogas.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "Endereços", description = "Endereços de entrega do cliente")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Listar os meus endereços")
    public ResponseEntity<ApiResponse<?>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(addressService.listByUser(currentUser)));
    }

    @PostMapping
    @Operation(summary = "Adicionar endereço de entrega")
    public ResponseEntity<ApiResponse<?>> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(addressService.create(request, currentUser), "Endereço adicionado"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover endereço")
    public ResponseEntity<ApiResponse<?>> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {
        addressService.delete(id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null, "Endereço removido"));
    }
}
