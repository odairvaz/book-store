package com.example.bookstore.security;

public interface Token {
    boolean isTokenFound();
    boolean isTokenExpired();
}
