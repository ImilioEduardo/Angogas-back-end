package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.zone.CreateZoneRequest;
import ao.angogas.backend.dto.response.zone.ZoneResponse;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.model.Zone;
import ao.angogas.backend.repository.ZoneRepository;
import ao.angogas.backend.service.ZoneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ZoneResponse> listActive() {
        return zoneRepository.findByActivaTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ZoneResponse create(CreateZoneRequest request) {
        Zone zone = Zone.builder()
                .nome(request.getNome())
                .municipio(request.getMunicipio())
                .coordenadas(toJson(request.getCoordenadas()))
                .build();
        return toResponse(zoneRepository.save(zone));
    }

    @Override
    public ZoneResponse toggleActive(UUID id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona não encontrada"));
        zone.setActiva(!zone.isActiva());
        return toResponse(zoneRepository.save(zone));
    }

    private ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .id(zone.getId())
                .nome(zone.getNome())
                .municipio(zone.getMunicipio())
                .activa(zone.isActiva())
                .coordenadas(fromJson(zone.getCoordenadas()))
                .build();
    }

    private String toJson(List<?> list) {
        try {
            return list == null ? null : objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<ZoneResponse.CoordenadasPonto> fromJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
