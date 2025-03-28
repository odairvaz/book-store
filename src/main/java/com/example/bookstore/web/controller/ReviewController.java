package com.example.bookstore.web.controller;

import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.service.IReviewService;
import com.example.bookstore.web.dto.ReviewDto;
import com.example.bookstore.web.mapper.ReviewMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static com.example.bookstore.web.controller.BookController.PAGE_TITLE_ATTRIBUTE;

@Controller
@RequestMapping("/api/v1/books")
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{bookId}/reviews")
    public String addReview(@PathVariable Long bookId, @ModelAttribute("review") ReviewDto reviewDto, Principal principal) {
        reviewService.addReview(bookId, reviewDto, principal);
        return "redirect:/api/v1/books/details/" + bookId;
    }

    @GetMapping("/reviews/edit/{reviewId}")
    public String editReview(Model model, @PathVariable Long reviewId) {
        Review review = reviewService.findById(reviewId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        ReviewDto reviewDto = ReviewMapper.convertToDto(review);
        model.addAttribute("review", reviewDto);
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Edit review");
        return "review/edit";
    }

    @PostMapping("/reviews/update/{reviewId}")
    public String updateReview(@PathVariable Long reviewId, ReviewDto reviewDto) {
        Review review = reviewService.findById(reviewId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        review.setComment(reviewDto.comment());
        review.setRating(reviewDto.rating());

        reviewService.save(review);

        return "redirect:/api/v1/books/details/" + review.getBook().getId();
    }

    @GetMapping("/reviews/delete/{reviewId}")
    public String deleteBook(@PathVariable Long reviewId) {
        Review review = reviewService.findById(reviewId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        reviewService.deleteById(reviewId);
        return "redirect:/api/v1/books/details/" + review.getBook().getId();
    }

}
