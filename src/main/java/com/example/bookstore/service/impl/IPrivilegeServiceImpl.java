package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.Privilege;
import com.example.bookstore.persistense.repository.IPrivilegeRepository;
import com.example.bookstore.service.IPrivilegeService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IPrivilegeServiceImpl implements IPrivilegeService {

    private final IPrivilegeRepository privilegeRepository;

    public IPrivilegeServiceImpl(IPrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    @Override
    public Optional<Privilege> findPrivilegesByName(String name) {
        return Optional.ofNullable(privilegeRepository.findByName(name));
    }

    @Override
    public Privilege createPrivilegeIfNotFound(String name) {
        return findPrivilegesByName(name).orElseGet(() -> {
            Privilege privilege = new Privilege(name);
            return privilegeRepository.save(privilege);
        });
    }

}
