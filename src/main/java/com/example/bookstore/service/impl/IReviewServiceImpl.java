package com.example.bookstore.service.impl;

import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.persistense.model.User;
import com.example.bookstore.persistense.repository.IBookRepository;
import com.example.bookstore.persistense.repository.IReviewRepository;
import com.example.bookstore.persistense.repository.IUserRepository;
import com.example.bookstore.security.core.userdetails.BookStoreUserDetail;
import com.example.bookstore.service.IReviewService;
import com.example.bookstore.web.dto.ReviewDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class IReviewServiceImpl implements IReviewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IReviewServiceImpl.class);

    private final IReviewRepository reviewRepository;
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;

    public IReviewServiceImpl(IReviewRepository reviewRepository, IBookRepository bookRepository, IUserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Review> findByBookId(Long bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        return reviewRepository.findById(reviewId);
    }

    @Override
    public void deleteById(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Review save(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    public void addReview(Long bookId, ReviewDto reviewDto, Principal principal) {
        LOGGER.info("Attempting to add a review for book ID: {}", bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    LOGGER.error("Book not found: {}", bookId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found with ID: " + bookId);
                });

        User user = getCurrentUser();
        LOGGER.info("User: {} is submitting a review for book ID: {}", user.getEmail(), bookId);

        Review review = createReview(book, user, reviewDto);
        reviewRepository.save(review);

        LOGGER.info("Review successfully added by user {} for book ID: {}", user.getEmail(), bookId);
    }

    @Override
    public List<Review> findByBookIdWithUser(Long bookId) {
        return reviewRepository.findByBookIdWithUser(bookId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            LOGGER.warn("Unauthorized access attempt: User must be logged in");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be logged in");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof BookStoreUserDetail userDetail)) {
            LOGGER.error("Invalid user principal detected");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal");
        }

        return userRepository.findByEmail(userDetail.getEmail())
                .orElseThrow(() -> {
                    LOGGER.error("User not found: {}", userDetail.getEmail());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + userDetail.getEmail());
                });
    }

    private Review createReview(Book book, User user, ReviewDto reviewDto) {
        Review review = new Review();
        review.setBook(book);
        review.setUser(user);
        review.setRating(reviewDto.rating());
        review.setComment(reviewDto.comment());
        return review;
    }

}
