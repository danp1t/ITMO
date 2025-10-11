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
        organization.validate();

        return organizationRepository.save(organization);
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    public boolean deleteOrganization(Long id) {
        Organization organization = organizationRepository.findById(id);
        if (organization != null) {
            organizationRepository.delete(organization);
            return true;
        }
        return false;
    }

    public Organization updateOrganization(Long id, OrganizationDTO updateDto) {
        // Находим существующую организацию
        Organization existingOrganization = organizationRepository.findById(id);
        if (existingOrganization == null) {
            return null;
        }

        // Обновляем только те поля, которые пришли в DTO
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

        // Валидируем организацию после обновления
        existingOrganization.validate();

        return organizationRepository.update(existingOrganization);
    }
}