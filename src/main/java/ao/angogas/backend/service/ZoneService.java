package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.zone.CreateZoneRequest;
import ao.angogas.backend.dto.response.zone.ZoneResponse;

import java.util.List;

public interface ZoneService {
    List<ZoneResponse> listActive();
    ZoneResponse create(CreateZoneRequest request);
    ZoneResponse toggleActive(java.util.UUID id);
}
