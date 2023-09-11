package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IBookService {

    Optional<Book> findById(Long id);

    Book save(Book book);

    void deleteById(Long id);

    Page<Book> findAll(Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
