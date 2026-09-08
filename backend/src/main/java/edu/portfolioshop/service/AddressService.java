package edu.portfolioshop.service;

import edu.portfolioshop.dto.AddressRequest;
import edu.portfolioshop.entities.Address;
import edu.portfolioshop.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addresses;

    public List<Address> findByUser(String userId) {
        return addresses.findByUserId(userId);
    }

    public Address findOwned(String id, String userId) {
        return addresses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NoSuchElementException("No address " + id + " for this account"));
    }

    public Address create(String userId, AddressRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        apply(address, request);
        return addresses.save(address);
    }

    public Address update(String id, String userId, AddressRequest request) {
        Address address = findOwned(id, userId);
        apply(address, request);
        return addresses.save(address);
    }

    public void delete(String id, String userId) {
        addresses.deleteByIdAndUserId(id, userId);
    }

    private void apply(Address address, AddressRequest request) {
        address.setLabel(request.label());
        address.setLine1(request.line1());
        address.setLine2(request.line2());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
    }
}
