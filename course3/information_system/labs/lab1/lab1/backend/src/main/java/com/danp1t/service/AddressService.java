package com.danp1t.service;

import com.danp1t.bean.Address;
import com.danp1t.repository.AddressRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class AddressService {

    @Inject
    private AddressRepository addressRepository;

    public Address createAddress(Address address) {
        address.validate();
        return addressRepository.save(address);
    }

    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    public Address getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    public Address updateAddress(Address address) {
        address.validate();

        Address existingAddress = addressRepository.findById(address.getId());
        if (existingAddress == null) {
            return null;
        }

        return addressRepository.update(address);
    }

    public boolean deleteAddress(Long id) {
        Address address = addressRepository.findById(id);
        if (address != null) {
            addressRepository.delete(address);
            return true;
        }
        return false;
    }
}