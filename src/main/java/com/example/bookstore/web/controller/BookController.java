package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.service.IBookService;
import com.example.bookstore.utils.ImageUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/books")
public class BookController {

    public static final String REDIRECT_BOOKS = "redirect:/api/v1/books";
    public static final String PAGE_TITLE_ATTRIBUTE = "pageTitle";
    private final IBookService bookService;
    private final int currentYear = Year.now().getValue();

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
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Add New Book");
        return "add";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute @Valid Book book, BindingResult bindingResult, @RequestParam("image") MultipartFile imageData) {
        if (bindingResult.hasErrors()) {
            return "add";
        }

        byte[] processedImageData = ImageUtils.processImageData(imageData);
        book.setBookCover(processedImageData);

        bookService.save(book);
        return REDIRECT_BOOKS;
    }

    @GetMapping("/details/{id}")
    public String showBookDetails(Model model, @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Book Details");
        if (book.getBookCover() != null) {
            String base64Image = Base64.getEncoder().encodeToString(book.getBookCover());
            model.addAttribute("base64Image", base64Image);
        }
        return "details";
    }

    @GetMapping("/edit/{id}")
    public String editBook(Model model, @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Edit Book");
        if (book.getBookCover() != null) {
            String base64Image = Base64.getEncoder().encodeToString(book.getBookCover());
            model.addAttribute("base64Image", base64Image);
        }
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id, Book book, @RequestParam("image") MultipartFile imageData) {
        Optional<Book> optionalBook = bookService.findById(id);

        if (optionalBook.isPresent()) {
            Book existingBook = optionalBook.get();

            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setPublisher(book.getPublisher());
            existingBook.setYear(book.getYear());
            existingBook.setPrice(book.getPrice());

            byte[] processedImageData = ImageUtils.processImageData(imageData);
            existingBook.setBookCover(processedImageData);

            bookService.save(existingBook);

            return REDIRECT_BOOKS;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteById(id);
        return REDIRECT_BOOKS;
    }

}
