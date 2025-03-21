package com.example.bookstore.service;

import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.web.dto.ReviewDto;

import java.util.List;

public interface IReviewService {

    List<Review> findByBookId(Long bookId);

    void addReview(Long bookId, ReviewDto reviewDto);

}
