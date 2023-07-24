package com.example.bookstore.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class BookDto {

    private Long id;

    @NotBlank
    private String title;
    @NotBlank
    private String author;
    private String published;
    @NotBlank
    @Min(1900)
    private int year;
    @NotBlank
    @Min(2)
    private double price;

    public BookDto(String title, String author, String published, int year, double price) {
        this.title = title;
        this.author = author;
        this.published = published;
        this.year = year;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
