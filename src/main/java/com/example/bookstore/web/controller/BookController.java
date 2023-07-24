package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.service.IBookService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;

@Controller
@RequestMapping("/api/v1/books")
public class BookController {

    private final IBookService bookService;
    int currentYear = Year.now().getValue();

    public BookController(IBookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String home(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "home";
    }

    @GetMapping("/new")
    public String addNewBook(Model model) {
        Book book = new Book();
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("book", book);
        model.addAttribute("pageTitle", "Add New Book");
        return "add";
    }

    @PostMapping("/save")
    public String saveBook(Book book) {
        bookService.save(book);
        return "redirect:/api/v1/books";
    }

    @GetMapping("/details/{id}")
    public String showBookDetails(Model model, @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        model.addAttribute("pageTitle", "Book Details");
        return "details";
    }

    @GetMapping("/edit/{id}")
    public String editBook(Model model, @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("pageTitle", "Edit Book");
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id, Book book) {
        bookService.findById(id).map(
                existingBook -> {
                    existingBook.setTitle(book.getTitle());
                    existingBook.setAuthor(book.getAuthor());
                    existingBook.setPublisher(book.getPublisher());
                    existingBook.setYear(book.getYear());
                    existingBook.setPrice(book.getPrice());
                    bookService.save(existingBook);
                    return "redirect:/api/v1/books";
                }
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return "redirect:/api/v1/books";
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
        return "redirect:/api/v1/books";
    }

}
