package com.danp1t.service;

import com.danp1t.model.Address;
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
            throw new IllegalArgumentException("Название улицы не может быть пустой строкой или null");
        }
        if (address.getZipCode() == null || address.getZipCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Индекс не может быть пустой строкой или null");
        }

        try {
            return addressRepository.save(address);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания адреса: " + e.getMessage(), e);
        }
    }

    public List<Address> getAllAddresses() {
        try {
            return addressRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения адресов: " + e.getMessage(), e);
        }
    }

    public Address getAddressById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть NULL");
        }

        try {
            Address address = addressRepository.findById(id);
            if (address == null) {
            }
            return address;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска адреса: " + e.getMessage(), e);
        }
    }

    public Address updateAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Адрес не может быть NULL");
        }
        if (address.getId() == null) {
            throw new IllegalArgumentException("Адрес ID не может быть NULL");
        }
        address.validate();

        Address existingAddress = addressRepository.findById(address.getId());
        if (existingAddress == null) {
            return null;
        }

        try {
            return addressRepository.update(address);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обновления адреса: " + e.getMessage(), e);
        }
    }

    public boolean deleteAddress(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть NULL");
        }

        try {
            addressRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка удаления адреса: " + e.getMessage(), e);
        }
    }
}