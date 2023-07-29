package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.service.IBookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;

@Controller
@RequestMapping("/api/v1/books")
public class BookController {

    public static final String REDIRECT_BOOKS = "redirect:/api/v1/books";
    private final IBookService bookService;
    int currentYear = Year.now().getValue();

    public BookController(IBookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String home(Model model, @Param("keyword") String keyword, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "3") int size) {
        Pageable paging = PageRequest.of(page - 1, size);
        Page<Book> pageBooks;

        if (StringUtils.hasLength(keyword)) {
            pageBooks = bookService.findByTitleContainingIgnoreCase(keyword, paging);
            model.addAttribute("keyword", keyword);
        } else {
            pageBooks = bookService.findAll(paging);
        }
        List<Book> books = pageBooks.getContent();
        model.addAttribute("books", books);
        model.addAttribute("pageSize", size);
        model.addAttribute("currentPage", pageBooks.getNumber() + 1);
        model.addAttribute("totalItems", pageBooks.getTotalElements());
        model.addAttribute("totalPages", pageBooks.getTotalPages());
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
        return REDIRECT_BOOKS;
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
                    return REDIRECT_BOOKS;
                }
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return REDIRECT_BOOKS;
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
        return REDIRECT_BOOKS;
    }

}
