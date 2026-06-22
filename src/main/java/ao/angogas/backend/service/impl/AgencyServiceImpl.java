package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.agency.CreateAgencyRequest;
import ao.angogas.backend.dto.response.agency.AgencyResponse;
import ao.angogas.backend.exception.BusinessException;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.model.Agency;
import ao.angogas.backend.repository.AgencyRepository;
import ao.angogas.backend.service.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {

    private final AgencyRepository agencyRepository;

    @Override
    public List<AgencyResponse> listAll() {
        return agencyRepository.findAllByOrderByNomeAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AgencyResponse create(CreateAgencyRequest request) {
        if (agencyRepository.existsByNif(request.getNif())) {
            throw new BusinessException("NIF já registado");
        }
        Agency agency = Agency.builder()
                .nome(request.getNome())
                .nif(request.getNif())
                .responsavel(request.getResponsavel())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .morada(request.getMorada())
                .build();
        return toResponse(agencyRepository.save(agency));
    }

    @Override
    public AgencyResponse toggleActive(UUID id) {
        Agency agency = agencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agência não encontrada"));
        agency.setActiva(!agency.isActiva());
        return toResponse(agencyRepository.save(agency));
    }

    private AgencyResponse toResponse(Agency a) {
        return AgencyResponse.builder()
                .id(a.getId())
                .nome(a.getNome())
                .nif(a.getNif())
                .responsavel(a.getResponsavel())
                .latitude(a.getLatitude())
                .longitude(a.getLongitude())
                .morada(a.getMorada())
                .activa(a.isActiva())
                .criadoEm(a.getCriadoEm())
                .build();
    }
}
