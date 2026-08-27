package com.booklibrary.booklibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booklibrary.booklibrary.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
}
