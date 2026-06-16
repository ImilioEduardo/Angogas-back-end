package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.zone.CreateZoneRequest;
import ao.angogas.backend.dto.response.zone.ZoneResponse;
import ao.angogas.backend.model.Zone;
import ao.angogas.backend.repository.ZoneRepository;
import ao.angogas.backend.service.ZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;

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
                .build();
        return toResponse(zoneRepository.save(zone));
    }

    private ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
                .id(zone.getId())
                .nome(zone.getNome())
                .municipio(zone.getMunicipio())
                .activa(zone.isActiva())
                .build();
    }
}
