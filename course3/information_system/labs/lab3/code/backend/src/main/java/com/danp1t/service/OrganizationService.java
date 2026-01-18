package com.danp1t.service;

import com.danp1t.dto.PaginatedResponse;
import com.danp1t.model.Organization;
import com.danp1t.dto.OrganizationDTO;
import com.danp1t.model.*;
import com.danp1t.repository.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private SessionFactory sessionFactory;

    public Organization createOrganization(Organization organization) {
        organization.validate();

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();

            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            });
            transaction = session.beginTransaction();

            checkOrganizationUniqueness(organization, null, session);
            saveRelatedEntities(organization, session);
            organizationRepository.save(organization, session);

            transaction.commit();
            return organization;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка создания организации: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void saveRelatedEntities(Organization organization, Session session) {
        if (organization.getCoordinates() != null) {
            if (organization.getCoordinates().getId() == null) {
                organizationRepository.saveCoordinates(organization.getCoordinates(), session);
            } else {
                Coordinates existingCoords = organizationRepository.findCoordinatesById(
                        organization.getCoordinates().getId(), session);
                if (existingCoords != null) {
                    existingCoords.setX(organization.getCoordinates().getX());
                    existingCoords.setY(organization.getCoordinates().getY());
                    organization.setCoordinates(existingCoords);
                } else {
                    organizationRepository.saveCoordinates(organization.getCoordinates(), session);
                }
            }
        }

        if (organization.getOfficialAddress() != null) {
            if (organization.getOfficialAddress().getId() == null) {
                organizationRepository.saveLocation(organization.getOfficialAddress(), session);
            } else {
                Location existingLocation = organizationRepository.findLocationById(
                        organization.getOfficialAddress().getId(), session);
                if (existingLocation != null) {
                    existingLocation.setX(organization.getOfficialAddress().getX());
                    existingLocation.setY(organization.getOfficialAddress().getY());
                    existingLocation.setZ(organization.getOfficialAddress().getZ());
                    existingLocation.setName(organization.getOfficialAddress().getName());
                    organization.setOfficialAddress(existingLocation);
                } else {
                    organizationRepository.saveLocation(organization.getOfficialAddress(), session);
                }
            }
        }

        if (organization.getPostalAddress() != null) {
            if (organization.getPostalAddress().getId() == null) {
                organizationRepository.saveAddress(organization.getPostalAddress(), session);
            } else {
                Address existingAddress = organizationRepository.findAddressById(
                        organization.getPostalAddress().getId(), session);
                if (existingAddress != null) {
                    existingAddress.setStreet(organization.getPostalAddress().getStreet());
                    existingAddress.setZipCode(organization.getPostalAddress().getZipCode());
                    organization.setPostalAddress(existingAddress);
                } else {
                    organizationRepository.saveAddress(organization.getPostalAddress(), session);
                }
            }
        }
    }

    public boolean deleteOrganization(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            boolean deleted = organizationRepository.delete(id, session);

            transaction.commit();
            return deleted;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка удаления организации: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Organization updateOrganization(Long id, OrganizationDTO updateDto) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            });
            transaction = session.beginTransaction();

            Organization existingOrganization = organizationRepository.findById(id, session);
            if (existingOrganization == null) {
                throw new IllegalArgumentException("Организация с ID " + id + " не найдена");
            }

            updateOrganizationFields(existingOrganization, updateDto);

            checkOrganizationUniquenessForUpdate(existingOrganization, id, session);

            existingOrganization.validate();

            Organization updated = organizationRepository.update(existingOrganization, session);

            transaction.commit();
            return updated;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Ошибка обновления организации: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    private void updateOrganizationFields(Organization organization, OrganizationDTO dto) {
        if (dto.getName() != null) {
            organization.setName(dto.getName());
        }
        if (dto.getAnnualTurnover() != null) {
            organization.setAnnualTurnover(dto.getAnnualTurnover());
        }
        if (dto.getEmployeesCount() != null) {
            organization.setEmployeesCount(dto.getEmployeesCount());
        }
        if (dto.getRating() != null) {
            organization.setRating(dto.getRating());
        }
        if (dto.getType() != null) {
            organization.setType(dto.getType());
        }
    }

    private void checkOrganizationUniqueness(Organization organization, Long excludeId, Session session) {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        if (name == null || coords == null || address == null) {
            throw new IllegalArgumentException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        boolean exists;
        if (excludeId == null) {
            exists = organizationRepository.existsByNameAndCoordinatesAndAddress(name, coords, address, session);
        } else {
            exists = organizationRepository.checkUniqueness(name, coords, address, excludeId, session);
        }

        if (exists) {
            throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже существует");
        }
    }

    private void checkOrganizationUniquenessForUpdate(Organization organization, Long excludeId, Session session) {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        if (name == null || coords == null || address == null) {
            throw new IllegalArgumentException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        boolean exists = organizationRepository.checkUniqueness(name, coords, address, excludeId, session);

        if (exists) {
            throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже существует");
        }
    }

    public Double calculateAverageRating() {
        return organizationRepository.calculateAverageRating();
    }

    public List<OrganizationDTO> findOrganizationsByNameStartingWith(String substring) {
        List<Organization> organizations = organizationRepository.findOrganizationsByNameStartingWith(substring);
        return organizations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private OrganizationDTO convertToDTO(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());

        if (organization.getAnnualTurnover() != null) {
            dto.setAnnualTurnover(organization.getAnnualTurnover());
        }

        if (organization.getEmployeesCount() != null) {
            dto.setEmployeesCount(organization.getEmployeesCount());
        }

        if (organization.getRating() != null) {
            dto.setRating(organization.getRating());
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
        List<Organization> organizations = organizationRepository.findOrganizationsByPostalAddressGreaterThan(minAddressId);
        return organizations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Organization mergeOrganizations(Long firstOrgId, Long secondOrgId, String newName, Long newAddressId) {
        return organizationRepository.mergeOrganizations(firstOrgId, secondOrgId, newName, newAddressId);
    }

    public Organization absorbOrganization(Long absorbingOrgId, Long absorbedOrgId) {
        return organizationRepository.absorbOrganization(absorbingOrgId, absorbedOrgId);
    }

    public PaginatedResponse<Organization> getAllOrganizationsWithPagination(
            int page, int size, String search, String type, String sortBy, String sortOrder) {

        int offset = (page - 1) * size;

        List<Organization> organizations = organizationRepository
                .findAllWithFilters(offset, size, search, type, sortBy, sortOrder);

        long totalCount = organizationRepository
                .countWithFilters(search, type);

        int totalPages = (int) Math.ceil((double) totalCount / size);

        return new PaginatedResponse<>(
                organizations,
                page,
                totalPages,
                totalCount,
                size
        );
    }
}