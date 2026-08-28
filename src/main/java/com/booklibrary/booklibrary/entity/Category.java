package com.booklibrary.booklibrary.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "categories")
@Data
public class Category {
  @Schema(description = "Unique identifier of the category", example = "1")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Schema(description = "Name of the category", example = "Fiction")
  @NotBlank(message = "Name is mandatory")
  @Column(nullable = false, unique = true)
  private String name;

  @Schema(description = "Description of the category", example = "Fictional and imaginative literary works")
  private String description;
}
