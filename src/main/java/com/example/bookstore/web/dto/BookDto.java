package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.Year;

public record BookDto(
        Long id,
        @NotBlank String title,
        @NotBlank String author,
        String publisher,
        @Min(1900) int year,
        @Min(2) double price,
        byte[] bookCover
) {
    public static BookDto empty() {
        return new BookDto(null, "", "", "", Year.now().getValue(), 0.0, null);
    }
}

