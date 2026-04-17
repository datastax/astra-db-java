package com.ibm.astra.demo.controller;

import com.ibm.astra.demo.books.Book;
import com.ibm.astra.demo.books.BookService;
import com.ibm.astra.demo.books.BookVectorSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "CRUD REST API for books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "List all books")
    public List<Book> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book found"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(schema = @Schema()))
    })
    public Book findById(@PathVariable String id) {
        return bookService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Book with id '%s' was not found".formatted(id)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new book")
    public Book create(@RequestBody Book book) {
        return bookService.create(book);
    }

    @PostMapping("/load")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Load the default dataset of books")
    public Map<String, Object> loadDefaultDataSet() {
        int inserted = bookService.loadDefaultDataSet();
        return Map.of(
                "status", "success",
                "inserted", inserted,
                "datasetSize", 30
        );
    }


    @DeleteMapping("/books")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    @Operation(summary = "Flush all books from the collection")
    public void flushBooks() {
        bookService.flush();
    }

    @PostMapping("/vsearch")
    @ResponseBody
    @Operation(summary = "Vector search for books")
    public List<Book> vectorSearch(@RequestBody BookVectorSearchRequest request) {
        return bookService.searchBooks(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing book")
    public Book update(@PathVariable String id, @RequestBody Book book) {
        return bookService.update(id, book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book")
    public void delete(@PathVariable String id) {
        bookService.delete(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleBadRequest(IllegalArgumentException exception) {
        return Map.of(
                "status", "error",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException exception) {
        return Map.of(
                "status", "error",
                "message", exception.getMessage()
        );
    }
}