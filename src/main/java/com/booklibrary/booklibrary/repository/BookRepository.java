package com.booklibrary.booklibrary.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.booklibrary.booklibrary.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

  List<Book> findByCategoryId(Long categoryId);

  List<Book> findByStockLessThan(Integer stock);

  @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
      + "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Book> searchByTitleOrAuthor(@Param("keyword") String keyword);
}
