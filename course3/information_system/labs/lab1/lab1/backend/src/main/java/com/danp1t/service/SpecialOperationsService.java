package com.danp1t.service;

import com.danp1t.bean.Organization;
import com.danp1t.bean.OrganizationDTO;
import com.danp1t.repository.SpecialOperationsRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class SpecialOperationsService {

    @Inject
    private SpecialOperationsRepository specialOperationsRepository;

    public Double calculateAverageRating() {
        try {
            return specialOperationsRepository.calculateAverageRating();
        } catch (Exception e) {
            throw e;
        }
    }

    public List<OrganizationDTO> findOrganizationsByNameStartingWith(String substring) {
        try {
            return specialOperationsRepository.findOrganizationsByNameStartingWith(substring);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<OrganizationDTO> findOrganizationsByPostalAddressGreaterThan(Long minAddressId) {
        try {
            return specialOperationsRepository.findOrganizationsByPostalAddressGreaterThan(minAddressId);
        } catch (Exception e) {
            throw e;
        }
    }

    public Organization mergeOrganizations(Long firstOrgId, Long secondOrgId, String newName, Long newAddressId) {
        try {
            return specialOperationsRepository.mergeOrganizations(firstOrgId, secondOrgId, newName, newAddressId);
        } catch (Exception e) {
            throw e;
        }
    }

    public Organization absorbOrganization(Long absorbingOrgId, Long absorbedOrgId) {
        try {
            return specialOperationsRepository.absorbOrganization(absorbingOrgId, absorbedOrgId);
        } catch (Exception e) {
            throw e;
        }
    }
}