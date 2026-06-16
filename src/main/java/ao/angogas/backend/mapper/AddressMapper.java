package ao.angogas.backend.mapper;

import ao.angogas.backend.dto.request.address.CreateAddressRequest;
import ao.angogas.backend.dto.response.address.AddressResponse;
import ao.angogas.backend.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "criadoEm", ignore = true)
    Address toEntity(CreateAddressRequest request);

    AddressResponse toResponse(Address address);
}
