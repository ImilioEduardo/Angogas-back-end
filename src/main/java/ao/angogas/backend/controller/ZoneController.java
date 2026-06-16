package ao.angogas.backend.controller;

import ao.angogas.backend.dto.request.zone.CreateZoneRequest;
import ao.angogas.backend.dto.response.ApiResponse;
import ao.angogas.backend.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Tag(name = "Zonas", description = "Gestão de zonas de entrega")
public class ZoneController {

    private final ZoneService zoneService;

    @GetMapping
    @Operation(summary = "Listar zonas activas")
    public ResponseEntity<ApiResponse<?>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.listActive()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[Admin] Criar nova zona")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateZoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(zoneService.create(request), "Zona criada"));
    }
}
