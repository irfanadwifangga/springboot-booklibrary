package com.booklibrary.booklibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Book Controller", description = "APIs for managing books in the library")
@RestController
@RequestMapping("/api/books")
public class BookController {
  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @Operation(summary = "Get all books", description = "Retrieve a list of all books in the library")
  @GetMapping
  public List<Book> getAllBooks() {
    return bookService.getAllBooks();
  }

  @Operation(summary = "Get book by ID", description = "Retrieve a book by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the book"),
      @ApiResponse(responseCode = "404", description = "Book not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Book> getBookById(@PathVariable Long id) {
    return ResponseEntity.ok(bookService.getBookById(id));
  }

  @Operation(summary = "Create a new book", description = "Add a new book to the library")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Book created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid book data")
  })
  @PostMapping
  public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
    Book savedBook = bookService.createBook(book);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
  }

  @Operation(summary = "Update an existing book", description = "Update the details of an existing book by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Book updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid book data"),
      @ApiResponse(responseCode = "404", description = "Book not found")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Book> updateBook(@PathVariable Long id, @Valid @RequestBody Book updatedBook) {
    return ResponseEntity.ok(bookService.updateBook(id, updatedBook));
  }

  @Operation(summary = "Delete a book", description = "Remove a book from the library by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Book not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build();
  }
}
