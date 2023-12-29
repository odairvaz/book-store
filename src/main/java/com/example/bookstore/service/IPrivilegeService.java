package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Privilege;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface IPrivilegeService {

    Optional<Privilege> findPrivilegesByName(String name);

    @Transactional
    Privilege createPrivilegeIfNotFound(String name);

}
