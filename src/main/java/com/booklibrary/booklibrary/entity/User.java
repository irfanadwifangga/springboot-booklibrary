package com.booklibrary.booklibrary.entity;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @Schema(description = "Role granted to the user", example = "USER")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  // Both nullable on purpose: existing rows just get NULL when this column is added,
  // no manual DB migration needed (unlike the NOT NULL "role" column above).
  @Schema(description = "Consecutive failed login attempts since the last successful login", example = "0")
  @Column
  private Integer failedLoginAttempts = 0;

  @Schema(description = "Account is locked until this time due to too many failed login attempts", example = "null")
  @Column
  private LocalDateTime lockedUntil;
}
