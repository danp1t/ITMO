package com.danp1t.backend.service;

import com.danp1t.backend.model.Role;
import com.danp1t.backend.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Role> findById(Integer id) {
        return roleRepository.findById(id);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(Integer id) {
        roleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Integer id) {
        return roleRepository.existsById(id);
    }
}