package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.agency.CreateAgencyRequest;
import ao.angogas.backend.dto.response.agency.AgencyResponse;

import java.util.List;
import java.util.UUID;

public interface AgencyService {
    List<AgencyResponse> listAll();
    AgencyResponse create(CreateAgencyRequest request);
    AgencyResponse toggleActive(UUID id);
}
