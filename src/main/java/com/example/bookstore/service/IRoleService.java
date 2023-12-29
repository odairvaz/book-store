package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Privilege;
import com.example.bookstore.persistense.model.Role;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

public interface IRoleService {

    Optional<Role> findRoleByName(String name);

    @Transactional
    Role createRoleIfNotFound(String name, Collection<Privilege> privileges);
}
