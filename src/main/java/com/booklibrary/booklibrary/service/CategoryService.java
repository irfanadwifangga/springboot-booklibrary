package com.booklibrary.booklibrary.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.CategoryRequest;
import com.booklibrary.booklibrary.dto.response.CategoryResponse;
import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.repository.CategoryRepository;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepository;

  public CategoryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Page<CategoryResponse> getAllCategories(Pageable pageable) {
    return categoryRepository.findAll(pageable).map(this::toResponse);
  }

  public CategoryResponse getCategoryById(Long id) {
    return categoryRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
  }

  public CategoryResponse createCategory(CategoryRequest request) {
    Category category = new Category();
    category.setName(request.getName());
    category.setDescription(request.getDescription());
    return toResponse(categoryRepository.save(category));
  }

  public CategoryResponse updateCategory(Long id, CategoryRequest request) {
    Category existingCategory = categoryRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Category not found with id " + id));
    existingCategory.setName(request.getName());
    existingCategory.setDescription(request.getDescription());
    return toResponse(categoryRepository.save(existingCategory));
  }

  public void deleteCategory(Long id) {
    categoryRepository.deleteById(id);
  }

  private CategoryResponse toResponse(Category category) {
    CategoryResponse response = new CategoryResponse();
    response.setId(category.getId());
    response.setName(category.getName());
    response.setDescription(category.getDescription());
    return response;
  }
}
