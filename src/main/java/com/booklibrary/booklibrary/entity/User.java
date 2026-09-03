package com.booklibrary.booklibrary.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
  @Schema(description = "Unique identifier of the user", example = "1")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Schema(description = "Username of the user", example = "john_doe")
  @NotEmpty(message = "Username is mandatory")
  @Column(nullable = false, unique = true)
  private String username;

  @Schema(description = "Password of the user", example = "password123")
  @NotEmpty(message = "Password is mandatory")
  @Column(nullable = false)
  private String password;
}
