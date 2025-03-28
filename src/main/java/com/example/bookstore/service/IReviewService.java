package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.web.dto.ReviewDto;
import org.springframework.data.repository.query.Param;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

public interface IReviewService {

    List<Review> findByBookId(Long bookId);

    Optional<Review> findById(Long reviewId);

    void deleteById(Long reviewId);

    Review save(Review review);

    void addReview(Long bookId, ReviewDto reviewDto, Principal principal);

    List<Review> findByBookIdWithUser(@Param("bookId") Long bookId);


}
