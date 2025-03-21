package com.example.bookstore.web.controller;


import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.persistense.model.Review;
import com.example.bookstore.service.IBookService;
import com.example.bookstore.service.IReviewService;
import com.example.bookstore.utils.ImageUtils;
import com.example.bookstore.web.dto.BookDto;
import com.example.bookstore.web.dto.ReviewDto;
import com.example.bookstore.web.mapper.BookMapper;
import com.example.bookstore.web.mapper.ReviewMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private static final String DEFAULT_PAGE_SIZE = "3";
    private static final String DEFAULT_PAGE_NUMBER = "1";
    private static final String DEFAULT_SORT_FIELD = "title";
    private static final String DEFAULT_SORT_DIRECTION = "asc";
    private final int currentYear = Year.now().getValue();

    private final IBookService bookService;
    private final IReviewService reviewService;

    public BookController(IBookService bookService, IReviewService reviewService) {
        this.bookService = bookService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public String home(Model model, @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
                       @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
                       @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sortField,
                       @RequestParam(defaultValue = DEFAULT_SORT_DIRECTION) String sortDirection,
                       @RequestParam(required = false) String keyword) {
        Pageable paging = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

        Page<Book> pageBooks;
        if (StringUtils.hasLength(keyword)) {
            pageBooks = bookService.findByTitleContainingIgnoreCase(keyword, paging);
            model.addAttribute("keyword", keyword);
        } else {
            pageBooks = bookService.findAll(paging);
        }
        addPaginationAttributes(model, pageBooks, size, sortField, sortDirection, keyword);
        return "home";
    }

    @GetMapping("/new")
    public String addNewBook(Model model) {
        model.addAttribute("currentYear", currentYear);
        model.addAttribute("book", BookDto.empty());
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Add New Book");
        return "add";
    }

    @PostMapping("/save")
    public String saveBook(@ModelAttribute @Valid BookDto bookDto, BindingResult bindingResult, @RequestParam("image") MultipartFile imageData) {
        if (bindingResult.hasErrors()) {
            return "add";
        }
        Book book = BookMapper.convertToEntity(bookDto);
        byte[] processedImageData = ImageUtils.processImageData(imageData);
        book.setBookCover(processedImageData);
        bookService.save(book);
        return REDIRECT_BOOKS;
    }

    @GetMapping("/details/{id}")
    public String showBookDetails(Model model, @PathVariable Long id) {
        Book book = bookService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        BookDto bookDto = BookMapper.convertToDto(book);
        model.addAttribute("book", bookDto);

        // Fetch Reviews for the Book
        ReviewMapper reviewMapper = new ReviewMapper();

        List<ReviewDto> reviews = reviewService.findByBookId(id)
                .stream()
                .map(reviewMapper::convertToDto)
                .toList();
        model.addAttribute("reviews", reviews);

        // Add an empty ReviewDto for form binding
        model.addAttribute("review", ReviewDto.empty());

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
        BookDto bookDto = BookMapper.convertToDto(book);
        model.addAttribute("book", bookDto);
        model.addAttribute("currentYear", currentYear);
        model.addAttribute(PAGE_TITLE_ATTRIBUTE, "Edit Book");
        if (book.getBookCover() != null) {
            String base64Image = Base64.getEncoder().encodeToString(book.getBookCover());
            model.addAttribute("base64Image", base64Image);
        }
        return "edit";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id, BookDto bookDto, @RequestParam("image") MultipartFile imageData) {
        Optional<Book> optionalBook = bookService.findById(id);
        if (optionalBook.isPresent()) {
            Book existingBook = optionalBook.get();
            existingBook.setTitle(bookDto.title());
            existingBook.setAuthor(bookDto.author());
            existingBook.setPublisher(bookDto.publisher());
            existingBook.setYear(bookDto.year());
            existingBook.setPrice(bookDto.price());
            if (!imageData.isEmpty()) {
                byte[] processedImageData = ImageUtils.processImageData(imageData);
                existingBook.setBookCover(processedImageData);
            }
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

    private void addPaginationAttributes(Model model, Page<Book> pageBooks, int size,
                                         String sortField, String sortDirection, String keyword) {
        model.addAttribute("books", pageBooks.getContent());
        model.addAttribute("currentPage", pageBooks.getNumber() + 1);
        model.addAttribute("totalItems", pageBooks.getTotalElements());
        model.addAttribute("totalPages", pageBooks.getTotalPages());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDirection", sortDirection);
        if (keyword != null) {
            model.addAttribute("keyword", keyword);
        }
    }

}
