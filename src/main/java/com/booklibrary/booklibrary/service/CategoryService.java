package com.booklibrary.booklibrary.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.repository.CategoryRepository;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public List<Category> getAllCategories() {
    return categoryRepository.findAll();
  }

  public Category getCategoryById(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
  }

  public Category createCategory(Category category) {
    return categoryRepository.save(category);
  }

  public Category updateCategory(Long id, Category updatedCategory) {
    Category existingCategory = getCategoryById(id);
    existingCategory.setName(updatedCategory.getName());
    existingCategory.setDescription(updatedCategory.getDescription());
    return categoryRepository.save(existingCategory);
  }

  public void deleteCategory(Long id) {
    categoryRepository.deleteById(id);
  }
}
