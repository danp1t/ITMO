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
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    @Inject
    private SessionFactory sessionFactory;

    public Organization createOrganization(Organization organization) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            });
            transaction = session.beginTransaction();

            checkOrganizationUniqueness(organization, session, null);

            organization.validate();

            if (organization.getCoordinates() != null) {
                if (organization.getCoordinates().getId() == null) {
                    session.persist(organization.getCoordinates());
                } else {
                    Coordinates existingCoords = session.find(Coordinates.class, organization.getCoordinates().getId());
                    if (existingCoords != null) {
                        existingCoords.setX(organization.getCoordinates().getX());
                        existingCoords.setY(organization.getCoordinates().getY());
                        organization.setCoordinates(existingCoords);
                    } else {
                        session.persist(organization.getCoordinates());
                    }
                }
            }

            if (organization.getOfficialAddress() != null) {
                if (organization.getOfficialAddress().getId() == null) {
                    session.persist(organization.getOfficialAddress());
                } else {
                    Location existingAddress = session.find(Location.class, organization.getOfficialAddress().getId());
                    if (existingAddress != null) {
                        existingAddress.setX(organization.getOfficialAddress().getX());
                        existingAddress.setY(organization.getOfficialAddress().getY());
                        existingAddress.setZ(organization.getOfficialAddress().getZ());
                        existingAddress.setName(organization.getOfficialAddress().getName());
                        organization.setOfficialAddress(existingAddress);
                    } else {
                        session.persist(organization.getOfficialAddress());
                    }
                }
            }

            if (organization.getPostalAddress() != null) {
                if (organization.getPostalAddress().getId() == null) {
                    session.persist(organization.getPostalAddress());
                } else {
                    Address existingAddress = session.find(Address.class, organization.getPostalAddress().getId());
                    if (existingAddress != null) {
                        existingAddress.setStreet(organization.getPostalAddress().getStreet());
                        existingAddress.setZipCode(organization.getPostalAddress().getZipCode());
                        organization.setPostalAddress(existingAddress);
                    } else {
                        session.persist(organization.getPostalAddress());
                    }
                }
            }

            session.persist(organization);
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

    public boolean deleteOrganization(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            Organization organization = session.get(Organization.class, id);
            if (organization != null) {
                session.remove(organization);
                transaction.commit();
                return true;
            } else {
                transaction.rollback();
                return false;
            }
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

    public Organization updateOrganization(Integer id, OrganizationDTO updateDto) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            session.doWork(connection -> {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            });
            transaction = session.beginTransaction();

            Organization existingOrganization = session.get(Organization.class, id);
            if (existingOrganization == null) {
                throw new IllegalArgumentException("Организация с ID " + id + " не найдена");
            }

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

            checkOrganizationUniqueness(existingOrganization, session, id);

            existingOrganization.validate();

            Organization mergedOrganization = session.merge(existingOrganization);
            transaction.commit();

            return mergedOrganization;
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

    private void checkOrganizationUniqueness(Organization organization, Session session, Integer excludeId) {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        if (name == null || coords == null || address == null) {
            throw new IllegalArgumentException("Для проверки уникальности должны быть заполнены: название, координаты и адрес");
        }

        StringBuilder hql = new StringBuilder();
        if (address.getId() != null) {
            hql.append("SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.id = :addressId");
        } else {
            hql.append("SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                    "AND o.coordinates.x = :x AND o.coordinates.y = :y " +
                    "AND o.postalAddress.street = :street " +
                    "AND (o.postalAddress.zipCode = :zipCode OR (o.postalAddress.zipCode IS NULL AND :zipCode IS NULL))");
        }

        if (excludeId != null) {
            hql.append(" AND o.id != :excludeId");
        }

        var query = session.createQuery(hql.toString(), Boolean.class)
                .setParameter("name", name)
                .setParameter("x", coords.getX())
                .setParameter("y", coords.getY());

        if (address.getId() != null) {
            query.setParameter("addressId", address.getId());
        } else {
            query.setParameter("street", address.getStreet())
                    .setParameter("zipCode", address.getZipCode());
        }

        if (excludeId != null) {
            query.setParameter("excludeId", excludeId);
        }

        Boolean exists = query.uniqueResult();
        if (Boolean.TRUE.equals(exists)) {
            throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                    "', координатами (" + coords.getX() + ", " + coords.getY() +
                    ") и адресом (улица: " + address.getStreet() +
                    (address.getZipCode() != null ? ", индекс: " + address.getZipCode() : "") +
                    ") уже существует");
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

    public PaginatedResponse<Organization> getAllOrganizationsWithPagination(
            int page, int size, String search, String type, String sortBy, String sortOrder) {

        try {
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

        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения организаций с пагинацией: " + e.getMessage(), e);
        }
    }
}