package com.example.bookstore.web.controller;

import com.example.bookstore.service.IReviewService;
import com.example.bookstore.web.dto.ReviewDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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
}
