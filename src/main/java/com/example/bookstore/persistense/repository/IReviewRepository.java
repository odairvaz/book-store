package com.example.bookstore.persistense.repository;

import com.example.bookstore.persistense.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    void delete(Review review);

    @Query("SELECT r FROM Review r JOIN FETCH r.user WHERE r.book.id = :bookId")
    List<Review> findByBookIdWithUser(@Param("bookId") Long bookId);

}