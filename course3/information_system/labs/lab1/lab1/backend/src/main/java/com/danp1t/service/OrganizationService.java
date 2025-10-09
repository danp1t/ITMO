package com.danp1t.service;

import com.danp1t.bean.Organization;
import com.danp1t.repository.OrganizationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    public Organization createOrganization(Organization organization) {
        System.out.println(organization.toString());
        organization.validate();

        return organizationRepository.save(organization);
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    public Organization getOrganizationById(Integer id) {
        return organizationRepository.findById(id);
    }
}