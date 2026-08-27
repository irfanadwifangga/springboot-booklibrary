package com.booklibrary.booklibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.repository.BookRepository;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public List<Book> getAllBooks() {
    return bookRepository.findAll();
  }

  public Book getBookById(Long id) {
    return bookRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Book not found with id " + id));
  }

  public Book createBook(Book book) {
    return bookRepository.save(book);
  }

  public Book updateBook(Long id, Book updatedBook) {
    Book existingBook = getBookById(id);
    existingBook.setTitle(updatedBook.getTitle());
    existingBook.setAuthor(updatedBook.getAuthor());
    existingBook.setIsbn(updatedBook.getIsbn());
    existingBook.setStock(updatedBook.getStock());
    return bookRepository.save(existingBook);
  }

  public void deleteBook(Long id) {
    bookRepository.deleteById(id);
  }
}
