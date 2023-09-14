package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUserRepository extends CrudRepository<User, Long>, PagingAndSortingRepository<User, Long> {

    User findByEmail(String email);

}
