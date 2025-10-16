package com.danp1t.service;

import com.danp1t.bean.Organization;
import com.danp1t.bean.OrganizationDTO;
import com.danp1t.repository.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    public Organization createOrganization(Organization organization) {

        try {
            organization.validate();

            return organizationRepository.save(organization);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create organization: " + e.getMessage(), e);
        }
    }

    public List<Organization> getAllOrganizations() {
        try {
            return organizationRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve organizations: " + e.getMessage(), e);
        }
    }

    public Organization getOrganizationById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            return organizationRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve organization: " + e.getMessage(), e);
        }
    }

    public boolean deleteOrganization(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            Organization organization = organizationRepository.findById(id);
            if (organization != null) {
                organizationRepository.delete(organization);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete organization: " + e.getMessage(), e);
        }
    }

    public Organization updateOrganization(Integer id, OrganizationDTO updateDto) {
        Organization existingOrganization = organizationRepository.findById(id);

        if (updateDto.getName() != null) {
            existingOrganization.setName(updateDto.getName());
        }
        if (updateDto.getAnnualTurnover() != null) {
            existingOrganization.setAnnualTurnover(updateDto.getAnnualTurnover());
        }
        if (updateDto.getEmployeesCount() != null) {
            existingOrganization.setEmployeesCount(updateDto.getEmployeesCount());
        }
        if (updateDto.getRating() != null) {
            existingOrganization.setRating(updateDto.getRating());
        }
        if (updateDto.getType() != null) {
            existingOrganization.setType(updateDto.getType());
        }
        existingOrganization.validate();

        try {
            return organizationRepository.update(existingOrganization);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update organization: " + e.getMessage(), e);
        }
    }

    public Organization updateOrganization(Organization organization) {
        organization.validate();

        try {
            return organizationRepository.update(organization);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update organization: " + e.getMessage(), e);
        }
    }
}