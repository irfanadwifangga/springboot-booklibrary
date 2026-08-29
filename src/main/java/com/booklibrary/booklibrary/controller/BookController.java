package com.booklibrary.booklibrary.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.dto.request.BookRequest;
import com.booklibrary.booklibrary.dto.response.BookResponse;
import com.booklibrary.booklibrary.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
  public Page<BookResponse> getAllBooks(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
    return bookService.getAllBooks(pageable);
  }

  @Operation(summary = "Search books", description = "Search books by a keyword matched against title or author")
  @GetMapping("/search")
  public List<BookResponse> searchBooks(
      @Parameter(description = "Keyword to match against title or author", example = "gatsby") @RequestParam String keyword) {
    return bookService.searchBooks(keyword);
  }

  @Operation(summary = "Get books by category", description = "Retrieve all books belonging to a given category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the books")
  })
  @GetMapping("/category/{categoryId}")
  public List<BookResponse> getBooksByCategory(@PathVariable Long categoryId) {
    return bookService.getBooksByCategory(categoryId);
  }

  @Operation(summary = "Get low stock books", description = "Retrieve books whose stock is below the given threshold")
  @GetMapping("/low-stock")
  public List<BookResponse> getLowStockBooks(
      @Parameter(description = "Stock threshold", example = "5") @RequestParam(defaultValue = "5") Integer threshold) {
    return bookService.getLowStockBooks(threshold);
  }

  @Operation(summary = "Get book by ID", description = "Retrieve a book by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the book"),
      @ApiResponse(responseCode = "404", description = "Book not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getBookById(@PathVariable Long id) {
    return ResponseEntity.ok(bookService.getBookById(id));
  }

  @Operation(summary = "Create a new book", description = "Add a new book to the library")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Book created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid book data"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @PostMapping
  public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
    BookResponse savedBook = bookService.createBook(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
  }

  @Operation(summary = "Update an existing book", description = "Update the details of an existing book by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Book updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid book data"),
      @ApiResponse(responseCode = "404", description = "Book or category not found")
  })
  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
    return ResponseEntity.ok(bookService.updateBook(id, request));
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
