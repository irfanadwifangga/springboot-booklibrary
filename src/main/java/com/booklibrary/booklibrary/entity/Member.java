package com.booklibrary.booklibrary.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "members")
@Data
public class Member {
  @Schema(description = "Unique identifier of the member", example = "1")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Schema(description = "Name of the member", example = "John Doe")
  @NotBlank(message = "Name is mandatory")
  @Column(nullable = false)
  private String name;

  @Schema(description = "Email of the member", example = "john.doe@example.com")
  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email should be valid")
  @Column(nullable = false, unique = true)
  private String email;

  @Schema(description = "Phone number of the member", example = "+62834567890")
  @NotBlank(message = "Phone number is mandatory")
  @Column(nullable = false)
  private String phoneNumber;
}
