package com.example.bookstore.web.mapper;

import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.web.dto.ReviewDto;

public class ReviewMapper {

    public ReviewDto convertToDto(Review review) {
        return new ReviewDto(review.getId(), review.getReviewerName(), review.getRating(),
                review.getComment(), review.getCreatedAt());
    }

}
