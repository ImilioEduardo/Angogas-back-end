package ao.angogas.backend.service;

import ao.angogas.backend.dto.request.address.CreateAddressRequest;
import ao.angogas.backend.dto.response.address.AddressResponse;
import ao.angogas.backend.model.User;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    AddressResponse create(CreateAddressRequest request, User currentUser);
    List<AddressResponse> listByUser(User currentUser);
    void delete(UUID id, User currentUser);
}
