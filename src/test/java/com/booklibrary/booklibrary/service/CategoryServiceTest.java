package com.booklibrary.booklibrary.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.booklibrary.booklibrary.dto.request.CategoryRequest;
import com.booklibrary.booklibrary.dto.response.CategoryResponse;
import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.exception.ResourceNotFoundException;
import com.booklibrary.booklibrary.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock
  private CategoryRepository categoryRepository;

  @InjectMocks
  private CategoryService categoryService;

  @Test
  void getCategoryById_whenExists_returnsCategoryResponse() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Fiction");
    category.setDescription("Fictional works");

    when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

    CategoryResponse result = categoryService.getCategoryById(1L);

    assertEquals(1L, result.getId());
    assertEquals("Fiction", result.getName());
    assertEquals("Fictional works", result.getDescription());
  }

  @Test
  void getCategoryById_whenNotFound_throwsResourceNotFoundException() {
    when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99L));
  }

  @Test
  void createCategory_savesAndReturnsResponse() {
    CategoryRequest request = new CategoryRequest();
    request.setName("Sci-Fi");
    request.setDescription("Science fiction works");

    Category saved = new Category();
    saved.setId(2L);
    saved.setName("Sci-Fi");
    saved.setDescription("Science fiction works");

    when(categoryRepository.save(any(Category.class))).thenReturn(saved);

    CategoryResponse result = categoryService.createCategory(request);

    assertEquals(2L, result.getId());
    assertEquals("Sci-Fi", result.getName());
    verify(categoryRepository).save(any(Category.class));
  }

  @Test
  void updateCategory_whenNotFound_throwsResourceNotFoundException() {
    when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

    CategoryRequest request = new CategoryRequest();
    request.setName("Updated");
    request.setDescription("Updated desc");

    assertThrows(ResourceNotFoundException.class,
        () -> categoryService.updateCategory(5L, request));
  }

  @Test
  void getAllCategories_returnsPagedResponse() {
    Category category = new Category();
    category.setId(1L);
    category.setName("Fiction");

    Pageable pageable = PageRequest.of(0, 10);
    Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);

    when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);

    Page<CategoryResponse> result = categoryService.getAllCategories(pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals("Fiction", result.getContent().get(0).getName());
  }
}
