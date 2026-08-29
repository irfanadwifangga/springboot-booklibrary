package com.booklibrary.booklibrary.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BookRequest {
  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Author is required")
  private String author;

  @NotBlank(message = "ISBN is required")
  @Pattern(regexp = "\\d{13}", message = "ISBN must be a 13-digit number")
  private String isbn;

  @NotNull(message = "Stock is required")
  @Min(value = 0, message = "Stock cannot be negative")
  private Integer stock;

  @NotNull(message = "Category ID is required")
  private Long categoryId;
}
