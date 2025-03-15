package com.example.bookstore.web.mapper;

import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.web.dto.BookDto;

public class BookMapper {

    public static BookDto convertToDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getYear(),
                book.getPrice(),
                book.getBookCover()
        );
    }

    public static Book convertToEntity(BookDto dto) {
        return new Book(
                dto.title(),
                dto.author(),
                dto.publisher(),
                dto.year(),
                dto.price()
        );
    }
}
