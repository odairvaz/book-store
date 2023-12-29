package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.Privilege;
import com.example.bookstore.persistense.model.Role;
import com.example.bookstore.persistense.repository.IRoleRepository;
import com.example.bookstore.service.IRoleService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class IRoleServiceImpl implements IRoleService {

    private final IRoleRepository roleRepository;

    public IRoleServiceImpl(IRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public Optional<Role> findRoleByName(String name) {
        return Optional.ofNullable(roleRepository.findByName(name));
    }

    @Override
    public Role createRoleIfNotFound(String name, Collection<Privilege> privileges) {
        return findRoleByName(name).orElseGet(() -> {
            Role role = new Role(name);
            role.setPrivileges(privileges);
            return roleRepository.save(role);
        });
    }

}
