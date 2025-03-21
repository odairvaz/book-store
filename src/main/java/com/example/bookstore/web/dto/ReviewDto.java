package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record ReviewDto(
        Long id,
        @NotBlank String reviewerName,
        @Min(1) @Max(5) int rating,
        String comment,
        LocalDateTime createdAt
) {
    public static ReviewDto empty() {
        return new ReviewDto(null, "", 1, "", LocalDateTime.now());
    }
}
