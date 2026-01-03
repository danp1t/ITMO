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
            transaction = session.beginTransaction();

            // Проверка уникальности на программном уровне
            checkOrganizationUniqueness(organization, session, null);

            organization.validate();

            // Обрабатываем связанные объекты
            // Если объект имеет ID, то он существует - используем merge
            // Если не имеет ID, то он новый - используем persist

            if (organization.getCoordinates() != null) {
                if (organization.getCoordinates().getId() == null) {
                    session.persist(organization.getCoordinates());
                } else {
                    // Загружаем существующие координаты
                    Coordinates existingCoords = session.find(Coordinates.class, organization.getCoordinates().getId());
                    if (existingCoords != null) {
                        // Обновляем значения
                        existingCoords.setX(organization.getCoordinates().getX());
                        existingCoords.setY(organization.getCoordinates().getY());
                        organization.setCoordinates(existingCoords);
                    } else {
                        // Если не найден, создаем новые
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
                        // Обновляем значения
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
                        // Обновляем значения
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
            transaction = session.beginTransaction();

            // Получаем существующую организацию
            Organization existingOrganization = session.get(Organization.class, id);
            if (existingOrganization == null) {
                throw new IllegalArgumentException("Организация с ID " + id + " не найдена");
            }

            // Обновляем поля
            if (updateDto.getName() != null) {
                existingOrganization.setName(updateDto.getName());
            }
            if (updateDto.getAnnualTurnover() != null) {
                existingOrganization.setAnnualTurnover(updateDto.getAnnualTurnover().floatValue());
            }
            if (updateDto.getEmployeesCount() != null) {
                existingOrganization.setEmployeesCount(updateDto.getEmployeesCount().longValue());
            }
            if (updateDto.getRating() != null) {
                existingOrganization.setRating(updateDto.getRating().intValue());
            }
            if (updateDto.getType() != null) {
                existingOrganization.setType(updateDto.getType());
            }

            // Проверка уникальности на программном уровне (исключая текущую организацию)
            checkOrganizationUniqueness(existingOrganization, session, id);

            existingOrganization.validate();

            // Сохраняем изменения
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

    /**
     * Проверяет уникальность организации по трем парам полей
     * @param excludeId ID организации для исключения (при обновлении)
     */
    private void checkOrganizationUniqueness(Organization organization, Session session, Integer excludeId) {
        String name = organization.getName();
        Coordinates coords = organization.getCoordinates();
        Address address = organization.getPostalAddress();

        // Проверка 1: (name, coordinates)
        if (name != null && coords != null) {
            StringBuilder hql1 = new StringBuilder(
                    "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                            "AND o.coordinates.x = :x AND o.coordinates.y = :y");

            if (excludeId != null) {
                hql1.append(" AND o.id != :excludeId");
            }

            var query1 = session.createQuery(hql1.toString(), Boolean.class)
                    .setParameter("name", name)
                    .setParameter("x", coords.getX())
                    .setParameter("y", coords.getY());

            if (excludeId != null) {
                query1.setParameter("excludeId", excludeId);
            }

            Boolean exists1 = query1.uniqueResult();
            if (Boolean.TRUE.equals(exists1)) {
                throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                        "' и координатами (" + coords.getX() + ", " + coords.getY() +
                        ") уже существует");
            }
        }

        // Проверка 2: (name, address)
        if (name != null && address != null && address.getId() != null) {
            StringBuilder hql2 = new StringBuilder(
                    "SELECT COUNT(o) > 0 FROM Organization o WHERE o.name = :name " +
                            "AND o.postalAddress.id = :addressId");

            if (excludeId != null) {
                hql2.append(" AND o.id != :excludeId");
            }

            var query2 = session.createQuery(hql2.toString(), Boolean.class)
                    .setParameter("name", name)
                    .setParameter("addressId", address.getId());

            if (excludeId != null) {
                query2.setParameter("excludeId", excludeId);
            }

            Boolean exists2 = query2.uniqueResult();
            if (Boolean.TRUE.equals(exists2)) {
                throw new IllegalArgumentException("Нарушение уникальности: организация с названием '" + name +
                        "' и адресом ID=" + address.getId() + " уже существует");
            }
        }

        // Проверка 3: (coordinates, address)
        if (coords != null && address != null && address.getId() != null) {
            StringBuilder hql3 = new StringBuilder(
                    "SELECT COUNT(o) > 0 FROM Organization o WHERE " +
                            "o.coordinates.x = :x AND o.coordinates.y = :y " +
                            "AND o.postalAddress.id = :addressId");

            if (excludeId != null) {
                hql3.append(" AND o.id != :excludeId");
            }

            var query3 = session.createQuery(hql3.toString(), Boolean.class)
                    .setParameter("x", coords.getX())
                    .setParameter("y", coords.getY())
                    .setParameter("addressId", address.getId());

            if (excludeId != null) {
                query3.setParameter("excludeId", excludeId);
            }

            Boolean exists3 = query3.uniqueResult();
            if (Boolean.TRUE.equals(exists3)) {
                throw new IllegalArgumentException("Нарушение уникальности: организация с координатами (" +
                        coords.getX() + ", " + coords.getY() +
                        ") и адресом ID=" + address.getId() + " уже существует");
            }
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

    public PaginatedResponse<Organization> getAllOrganizationsWithPagination(
            int page, int size, String search, String type, String sortBy, String sortOrder) {

        try {
            // Вычисляем offset для запроса
            int offset = (page - 1) * size;

            // Получаем организации с фильтрами и пагинацией
            List<Organization> organizations = organizationRepository
                    .findAllWithFilters(offset, size, search, type, sortBy, sortOrder);

            // Получаем общее количество для фильтров
            long totalCount = organizationRepository
                    .countWithFilters(search, type);

            // Вычисляем общее количество страниц
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