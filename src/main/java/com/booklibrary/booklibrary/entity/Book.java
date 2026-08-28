package com.booklibrary.booklibrary.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Entity
@Table(name = "books")
@Data
public class Book {
  @Schema(description = "Unique identifier of the book", example = "1")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Schema(description = "Title of the book", example = "The Great Gatsby")
  @NotBlank(message = "Title is mandatory")
  @Column(nullable = false)
  private String title;

  @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
  @NotBlank(message = "Author is mandatory")
  @Column(nullable = false)
  private String author;

  @Schema(description = "ISBN of the book", example = "9783161484100")
  @NotBlank(message = "ISBN is mandatory")
  @Pattern(regexp = "\\d{13}", message = "ISBN must be a 13-digit number")
  @Column(unique = true)
  private String isbn;

  @Schema(description = "Available stock of the book", example = "5")
  @NotNull(message = "Stock is mandatory")
  @Min(value = 0, message = "Stock cannot be negative")
  private Integer stock;

  @Schema(description = "Category the book belongs to")
  @NotNull(message = "Category is mandatory")
  @ManyToOne
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;
}
