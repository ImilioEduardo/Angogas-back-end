package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.agency.CreateAgencyRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.service.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
@Tag(name = "Agências", description = "Gestão de agências fornecedoras de gás")
public class AgencyController {

    private final AgencyService agencyService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Listar todas as agências")
    public ResponseEntity<ApiResponse<?>> listAll() {
        return ResponseEntity.ok(ApiResponse.ok(agencyService.listAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Criar nova agência")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateAgencyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(agencyService.create(request), "Agência criada com sucesso"));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Activar/desactivar agência")
    public ResponseEntity<ApiResponse<?>> toggle(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(agencyService.toggleActive(id), "Estado da agência actualizado"));
    }
}
