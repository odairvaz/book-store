package com.example.bookstore.web.dto;

import com.example.bookstore.persistense.model.User;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        User user,
        @Min(1) @Max(5) int rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewDto empty() {
        return new ReviewDto(null, new User(), 1, "", LocalDateTime.now());
    }
}
