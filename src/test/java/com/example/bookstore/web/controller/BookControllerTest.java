package com.example.bookstore.web.controller;

import com.example.bookstore.persistense.model.Book;
import com.example.bookstore.service.IBookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookControllerTest {

    @Mock
    private IBookService bookService;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    Book book;
    MockMultipartFile imageFile;
    Long id = 1L;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();

        imageFile = new MockMultipartFile("image", "image.png", "image/png", "base64ImageData".getBytes());

        book = new Book();
        book.setTitle("Effective Java");
        book.setAuthor("Joshua Bloch");
        book.setBookCover("base64ImageData".getBytes());
        book.setYear(2023);
        book.setPrice(2.0);
    }

    @Test
    void givenKeyword_whenHome_thenReturnsHomePage() throws Exception {
        String keyword = "Spring";
        int page = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(0, size);

        Page<Book> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(Collections.emptyList());
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getTotalElements()).thenReturn(0L);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(bookService.findByTitleContainingIgnoreCase(eq(keyword), any())).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/books")
                        .param("keyword", keyword)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("books", "pageSize", "currentPage", "totalItems", "totalPages"))
                .andExpect(model().attribute("keyword", keyword));
        verify(bookService).findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void givenNoKeyword_whenHome_thenReturnsHomePage() throws Exception {
        int page = 1;
        int size = 3;
        Pageable pageable = PageRequest.of(0, size);

        Page<Book> mockPage = mock(Page.class);
        when(mockPage.getContent()).thenReturn(Collections.emptyList());
        when(mockPage.getNumber()).thenReturn(0);
        when(mockPage.getTotalElements()).thenReturn(0L);
        when(mockPage.getTotalPages()).thenReturn(1);
        when(bookService.findAll(any())).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/books")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("books", "pageSize", "currentPage", "totalItems", "totalPages"));
        verify(bookService).findAll(pageable);
    }

    @Test
    void givenBook_whenSaveBook_thenRedirectsToBooks() throws Exception {
        mockMvc.perform(multipart("/api/v1/books/save")
                                .file(imageFile)
                                .param("title", book.getTitle())
                                .param("author", book.getAuthor())
                                .param("year", String.valueOf(book.getYear()))
                                .param("price", String.valueOf(book.getPrice())))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/api/v1/books"));
    }

    @Test
    void givenInvalidBook_whenSaveBook_thenRedirectToAdd() throws Exception {
        Book invalidBook = new Book();
        mockMvc.perform(multipart("/api/v1/books/save")
                        .file(imageFile)
                        .param("title", invalidBook.getTitle())
                        .param("author", invalidBook.getAuthor())
                        .param("year", String.valueOf(invalidBook.getYear()))
                        .param("price", String.valueOf(invalidBook.getPrice())))
                .andExpect(view().name("add"));
    }

    @Test
    void givenAddNewBookRequest_thenReturnsAddNewBookPage() {
        Model model = mock(Model.class);
        BookController bookController = new BookController(bookService);
        String viewName = bookController.addNewBook(model);

        verify(model).addAttribute(eq("currentYear"), anyInt());
        verify(model).addAttribute(eq("book"), any(Book.class));
        verify(model).addAttribute((BookController.PAGE_TITLE_ATTRIBUTE), ("Add New Book"));

        assertEquals("add", viewName);
    }

    @Test
    void givenBook_whenShowBookDetails_thenReturnsBookDetailsPage() throws Exception {
        when(bookService.findById(id)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/v1/books/details/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("details"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    void givenBook_whenEditBook_thenReturnsEditBookPage() throws Exception {
        when(bookService.findById(id)).thenReturn(Optional.of(book));

        mockMvc.perform(get("/api/v1/books/edit/{id}", id))
                .andExpect(status().isOk())
                .andExpect(view().name("edit"))
                .andExpect(model().attributeExists("book", "currentYear"));
    }

    @Test
    void givenBook_whenUpdateBook_thenRedirectsToBooks() throws Exception {
        when(bookService.findById(id)).thenReturn(Optional.of(book));

        mockMvc.perform(multipart("/api/v1/books/update/{id}", id)
                    .file(imageFile)
                    .param("title", book.getTitle())
                    .param("author", book.getAuthor())
                    .param("year", String.valueOf(book.getYear()))
                    .param("price", String.valueOf(book.getPrice())))
                .andExpect(view().name("redirect:/api/v1/books"));
    }

    @Test
    void givenNonExistentBook_whenUpdateBook_thenThrowsResponseStatusException() {
        when(bookService.findById(anyLong())).thenReturn(Optional.empty());
        BookController bookController = new BookController(bookService);

        assertThrows(ResponseStatusException.class, () -> bookController.updateBook(id, new Book(), null));
        verify(bookService).findById(id);
    }

    @Test
    void givenExistingBook_whenDeleteBook_thenRedirectsToBooks() {
        doNothing().when(bookService).deleteById(id);
        BookController bookController = new BookController(bookService);
        String result = bookController.deleteBook(id);

        verify(bookService).deleteById(id);
        assertEquals(BookController.REDIRECT_BOOKS, result);
    }

}
