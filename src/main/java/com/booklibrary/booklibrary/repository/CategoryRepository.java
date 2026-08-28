package com.booklibrary.booklibrary.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booklibrary.booklibrary.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
