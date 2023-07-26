package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Book;

import java.util.List;
import java.util.Optional;

public interface IBookService {


    Optional<Book> findById(Long id);

    Book save(Book book);

    void deleteById(Long id);

    Iterable<Book> findAll();

    List<Book> findByTitleContainingIgnoreCase(String keyword);
}
