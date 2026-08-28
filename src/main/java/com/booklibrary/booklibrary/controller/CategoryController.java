package com.booklibrary.booklibrary.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.entity.Category;
import com.booklibrary.booklibrary.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Category Controller", description = "APIs for managing book categories in the library")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @Operation(summary = "Get all categories", description = "Retrieve a list of all book categories")
  @GetMapping
  public List<Category> getAllCategories() {
    return categoryService.getAllCategories();
  }

  @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully retrieved the category"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
    return ResponseEntity.ok(categoryService.getCategoryById(id));
  }

  @Operation(summary = "Create a new category", description = "Add a new book category")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Category created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid category data")
  })
  @PostMapping
  public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
    Category savedCategory = categoryService.createCategory(category);
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
  }

  @Operation(summary = "Update an existing category", description = "Update the details of an existing category by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Category updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid category data"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category updatedCategory) {
    return ResponseEntity.ok(categoryService.updateCategory(id, updatedCategory));
  }

  @Operation(summary = "Delete a category", description = "Remove a book category by its ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
      @ApiResponse(responseCode = "404", description = "Category not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
    categoryService.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
