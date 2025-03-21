package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.persistense.repository.IBookRepository;
import com.example.bookstore.persistense.repository.IReviewRepository;
import com.example.bookstore.service.IReviewService;
import com.example.bookstore.web.dto.ReviewDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class IReviewServiceImpl implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IBookRepository bookRepository;

    public IReviewServiceImpl(IReviewRepository reviewRepository, IBookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
    }


    @Override
    public List<Review> findByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public void addReview(Long bookId, ReviewDto reviewDto) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Review review = new Review();
        review.setBook(book);
        review.setReviewerName(reviewDto.reviewerName());
        review.setRating(reviewDto.rating());
        review.setComment(reviewDto.comment());

        reviewRepository.save(review);
    }

}
