package ao.angogas.backend.service.impl;

import ao.angogas.backend.dto.request.address.CreateAddressRequest;
import ao.angogas.backend.dto.response.address.AddressResponse;
import ao.angogas.backend.exception.ResourceNotFoundException;
import ao.angogas.backend.mapper.AddressMapper;
import ao.angogas.backend.model.Address;
import ao.angogas.backend.model.User;
import ao.angogas.backend.repository.AddressRepository;
import ao.angogas.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse create(CreateAddressRequest request, User currentUser) {
        if (request.isPredefinido()) {
            addressRepository.findByUserIdOrderByPredefinidoDesc(currentUser.getId())
                    .forEach(a -> {
                        a.setPredefinido(false);
                        addressRepository.save(a);
                    });
        }

        Address address = addressMapper.toEntity(request);
        address.setUser(currentUser);
        return addressMapper.toResponse(addressRepository.save(address));
    }

    @Override
    public List<AddressResponse> listByUser(User currentUser) {
        return addressRepository.findByUserIdOrderByPredefinidoDesc(currentUser.getId())
                .stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id, User currentUser) {
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
        addressRepository.delete(address);
    }
}
