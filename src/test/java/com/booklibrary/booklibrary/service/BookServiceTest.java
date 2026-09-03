package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.booklibrary.booklibrary.dto.request.BookRequest;
import com.booklibrary.booklibrary.dto.response.BookResponse;
import com.booklibrary.booklibrary.entity.Book;
import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
import com.booklibrary.booklibrary.repository.BookRepository;
import com.booklibrary.booklibrary.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private BookService bookService;

  private Category fictionCategory() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Fiction");
    return category;
  }

  private Book sampleBook() {
    Book book = new Book();
    book.setId(1L);
    book.setTitle("The Great Gatsby");
    book.setAuthor("F. Scott Fitzgerald");
    book.setIsbn("9783161484100");
    book.setStock(5);
    book.setCategory(fictionCategory());
    return book;
  }

  @Test
  void getBookById_whenExists_returnsBookResponse() {
    when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook()));

    BookResponse result = bookService.getBookById(1L);

    assertEquals(1L, result.getId());
    assertEquals("The Great Gatsby", result.getTitle());
    assertEquals(1L, result.getCategoryId());
  }

  @Test
  void getBookById_whenNotFound_throwsResourceNotFoundException() {
    when(bookRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(99L));
  }

  @Test
  void createBook_whenCategoryExists_savesAndReturnsResponse() {
    BookRequest request = new BookRequest();
    request.setTitle("Dune");
    request.setAuthor("Frank Herbert");
    request.setIsbn("9780441172719");
    request.setStock(3);
    request.setCategoryId(1L);

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(fictionCategory()));
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
      Book saved = invocation.getArgument(0);
      saved.setId(2L);
      return saved;
    });

    BookResponse result = bookService.createBook(request);

    assertEquals(2L, result.getId());
    assertEquals("Dune", result.getTitle());
    assertEquals(1L, result.getCategoryId());
  }

  @Test
  void createBook_whenCategoryNotFound_throwsResourceNotFoundException() {
    BookRequest request = new BookRequest();
    request.setTitle("Dune");
    request.setAuthor("Frank Herbert");
    request.setIsbn("9780441172719");
    request.setStock(3);
    request.setCategoryId(99L);

    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(request));
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void updateBook_whenNotFound_throwsResourceNotFoundException() {
    when(bookRepository.findById(5L)).thenReturn(Optional.empty());

    BookRequest request = new BookRequest();
    request.setTitle("Updated");
    request.setAuthor("Updated Author");
    request.setIsbn("9780000000000");
    request.setStock(1);
    request.setCategoryId(1L);

    assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(5L, request));
  }

  @Test
  void deleteBook_callsRepositoryDeleteById() {
    bookService.deleteBook(1L);

    verify(bookRepository).deleteById(1L);
  }

  @Test
  void searchBooks_returnsMappedList() {
    when(bookRepository.searchByTitleOrAuthor("gatsby")).thenReturn(List.of(sampleBook()));

    List<BookResponse> result = bookService.searchBooks("gatsby");

    assertEquals(1, result.size());
    assertEquals("The Great Gatsby", result.get(0).getTitle());
  }

  @Test
  void getBooksByCategory_returnsMappedList() {
    when(bookRepository.findByCategoryId(1L)).thenReturn(List.of(sampleBook()));

    List<BookResponse> result = bookService.getBooksByCategory(1L);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getCategoryId());
  }

  @Test
  void getLowStockBooks_returnsMappedList() {
    when(bookRepository.findByStockLessThan(10)).thenReturn(List.of(sampleBook()));

    List<BookResponse> result = bookService.getLowStockBooks(10);

    assertEquals(1, result.size());
  }

  @Test
  void getAllBooks_returnsPagedResponse() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Book> bookPage = new PageImpl<>(List.of(sampleBook()), pageable, 1);

    when(bookRepository.findAll(pageable)).thenReturn(bookPage);

    Page<BookResponse> result = bookService.getAllBooks(pageable);

    assertEquals(1, result.getTotalElements());
  }
}
