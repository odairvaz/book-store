package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

}