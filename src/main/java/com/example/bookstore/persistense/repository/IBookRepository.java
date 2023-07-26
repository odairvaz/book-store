package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.Book;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IBookRepository extends CrudRepository<Book, Long>, PagingAndSortingRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String keyword);
}
