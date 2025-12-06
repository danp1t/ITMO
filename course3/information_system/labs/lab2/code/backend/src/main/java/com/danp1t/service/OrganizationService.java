package com.danp1t.service;

import com.danp1t.model.Organization;
import com.danp1t.dto.OrganizationDTO;
import com.danp1t.repository.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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

    public Double calculateAverageRating() {
        try {
            return organizationRepository.calculateAverageRating();
        } catch (Exception e) {
            throw e;
        }
    }

    public List<OrganizationDTO> findOrganizationsByNameStartingWith(String substring) {
        try {
            List<Organization> organizations = organizationRepository.findOrganizationsByNameStartingWith(substring);
            return organizations.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw e;
        }
    }

    private OrganizationDTO convertToDTO(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());

        if (organization.getAnnualTurnover() != null) {
            dto.setAnnualTurnover(organization.getAnnualTurnover().floatValue());
        }

        if (organization.getEmployeesCount() != null) {
            dto.setEmployeesCount(organization.getEmployeesCount().longValue());
        }

        if (organization.getRating() != null) {
            dto.setRating(organization.getRating().intValue());
        }

        dto.setType(organization.getType());

        if (organization.getCoordinates() != null) {
            dto.setCoordinates(organization.getCoordinates().getId());
        }

        if (organization.getOfficialAddress() != null) {
            dto.setOfficialAddress(organization.getOfficialAddress().getId());
        }

        if (organization.getPostalAddress() != null) {
            dto.setPostalAddress(organization.getPostalAddress().getId());
        }

        return dto;
    }

    public List<OrganizationDTO> findOrganizationsByPostalAddressGreaterThan(Long minAddressId) {
        try {
            List<Organization> organizations =  organizationRepository.findOrganizationsByPostalAddressGreaterThan(minAddressId);
            return organizations.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw e;
        }
    }

    public Organization mergeOrganizations(Long firstOrgId, Long secondOrgId, String newName, Long newAddressId) {
        try {
            return organizationRepository.mergeOrganizations(firstOrgId, secondOrgId, newName, newAddressId);
        } catch (Exception e) {
            throw e;
        }
    }

    public Organization absorbOrganization(Long absorbingOrgId, Long absorbedOrgId) {
        try {
            return organizationRepository.absorbOrganization(absorbingOrgId, absorbedOrgId);
        } catch (Exception e) {
            throw e;
        }
    }
}