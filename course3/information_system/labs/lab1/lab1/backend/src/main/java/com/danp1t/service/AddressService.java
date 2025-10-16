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

        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        if (address.getZipCode() == null || address.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code cannot be null or empty");
        }

        try {
            Address savedAddress = addressRepository.save(address);
            return savedAddress;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create address: " + e.getMessage(), e);
        }
    }

    public List<Address> getAllAddresses() {
        try {
            List<Address> addresses = addressRepository.findAll();
            return addresses;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve addresses: " + e.getMessage(), e);
        }
    }

    public Address getAddressById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            Address address = addressRepository.findById(id);
            if (address == null) {
            }
            return address;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve address: " + e.getMessage(), e);
        }
    }

    public Address updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address.getId() == null) {
            throw new IllegalArgumentException("Address ID cannot be null for update");
        }
        address.validate();

        Address existingAddress = addressRepository.findById(address.getId());
        if (existingAddress == null) {
            return null;
        }

        try {
            Address updatedAddress = addressRepository.update(address);
            return updatedAddress;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update address: " + e.getMessage(), e);
        }
    }

    public boolean deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            addressRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete address: " + e.getMessage(), e);
        }
    }
}