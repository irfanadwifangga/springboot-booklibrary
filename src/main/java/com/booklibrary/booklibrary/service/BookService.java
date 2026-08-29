package com.booklibrary.booklibrary.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.entity.Category;
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

  public Page<Book> getAllBooks(Pageable pageable) {
    return bookRepository.findAll(pageable);
  }

  public Book getBookById(Long id) {
    return bookRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Book not found with id " + id));
  }

  public Book createBook(Book book) {
    book.setCategory(resolveCategory(book.getCategory()));
    return bookRepository.save(book);
  }

  public Book updateBook(Long id, Book updatedBook) {
    Book existingBook = getBookById(id);
    existingBook.setTitle(updatedBook.getTitle());
    existingBook.setAuthor(updatedBook.getAuthor());
    existingBook.setIsbn(updatedBook.getIsbn());
    existingBook.setStock(updatedBook.getStock());
    existingBook.setCategory(resolveCategory(updatedBook.getCategory()));
    return bookRepository.save(existingBook);
  }

  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }

  public List<Book> searchBooks(String keyword) {
    return bookRepository.searchByTitleOrAuthor(keyword);
  }

  public List<Book> getBooksByCategory(Long categoryId) {
    return bookRepository.findByCategoryId(categoryId);
  }

  public List<Book> getLowStockBooks(Integer threshold) {
    return bookRepository.findByStockLessThan(threshold);
  }

  private Category resolveCategory(Category category) {
    if (category == null || category.getId() == null) {
      throw new RuntimeException("Category is mandatory");
    }
    return categoryRepository.findById(category.getId())
        .orElseThrow(() -> new RuntimeException("Category not found with id " + category.getId()));
  }
}
