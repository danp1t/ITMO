package com.danp1t.service;

import com.danp1t.model.Organization;
import com.danp1t.dto.OrganizationDTO;
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
            throw new RuntimeException("Ошибка создания организации: " + e.getMessage(), e);
        }
    }

    public List<Organization> getAllOrganizations() {
        try {
            return organizationRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения организаций: " + e.getMessage(), e);
        }
    }

    public boolean deleteOrganization(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
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
            throw new RuntimeException("Ошибка удаления организации: " + e.getMessage(), e);
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
            throw new RuntimeException("Ошибка обновления организации: " + e.getMessage(), e);
        }
    }
}