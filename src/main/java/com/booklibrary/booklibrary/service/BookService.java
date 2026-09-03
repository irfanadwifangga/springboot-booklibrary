package com.booklibrary.booklibrary.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.BookRequest;
import com.booklibrary.booklibrary.dto.response.BookResponse;
import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
import com.booklibrary.booklibrary.repository.BookRepository;
import com.booklibrary.booklibrary.repository.CategoryRepository;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final CategoryRepository categoryRepository;

  public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
    this.bookRepository = bookRepository;
    this.categoryRepository = categoryRepository;
  }

  public Page<BookResponse> getAllBooks(Pageable pageable) {
    return bookRepository.findAll(pageable).map(this::toResponse);
  }

  public BookResponse getBookById(Long id) {
    return bookRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
  }

  public BookResponse createBook(BookRequest request) {
    Book book = new Book();
    book.setTitle(request.getTitle());
    book.setAuthor(request.getAuthor());
    book.setIsbn(request.getIsbn());
    book.setStock(request.getStock());
    Category category = resolveCategory(request.getCategoryId());
    book.setCategory(category);
    return toResponse(bookRepository.save(book));
  }

  public BookResponse updateBook(Long id, BookRequest request) {
    Book existingBook = bookRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    Category category = resolveCategory(request.getCategoryId());
    existingBook.setCategory(category);
    existingBook.setTitle(request.getTitle());
    existingBook.setAuthor(request.getAuthor());
    existingBook.setIsbn(request.getIsbn());
    existingBook.setStock(request.getStock());

    return toResponse(bookRepository.save(existingBook));
  }

  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }

  public List<BookResponse> searchBooks(String keyword) {
    return bookRepository.searchByTitleOrAuthor(keyword).stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<BookResponse> getBooksByCategory(Long categoryId) {
    return bookRepository.findByCategoryId(categoryId).stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<BookResponse> getLowStockBooks(Integer threshold) {
    return bookRepository.findByStockLessThan(threshold).stream().map(this::toResponse).collect(Collectors.toList());
  }

  private Category resolveCategory(Long categoryId) {
    if (categoryId == null) {
      throw new BadRequestException("Category is mandatory");
    }
    return categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
  }

  private BookResponse toResponse(Book book) {
    BookResponse response = new BookResponse();
    response.setId(book.getId());
    response.setTitle(book.getTitle());
    response.setAuthor(book.getAuthor());
    response.setIsbn(book.getIsbn());
    response.setStock(book.getStock());
    response.setCategoryId(book.getCategory().getId());
    return response;
  }
}
