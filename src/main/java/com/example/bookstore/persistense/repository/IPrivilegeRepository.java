package com.example.bookstore.persistense.repository;


import com.example.bookstore.persistense.model.Privilege;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IPrivilegeRepository extends CrudRepository<Privilege, Long> {

    Privilege findByName(String name);

    void delete(Privilege privilege);
}
