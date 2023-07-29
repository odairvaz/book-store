package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IBookRepository extends CrudRepository<Book, Long>, PagingAndSortingRepository<Book, Long> {

    Page<Book> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
