package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends CrudRepository<Role, Long> {

    Role findByName(String name);
}
