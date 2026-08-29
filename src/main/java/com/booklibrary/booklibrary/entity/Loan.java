package com.booklibrary.booklibrary.entity;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "loans")
@Data
public class Loan {
  @Schema(description = "Unique identifier of the loan", example = "1")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "Book is mandatory")
  @ManyToOne
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @NotNull(message = "Member is mandatory")
  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Schema(description = "Date borrowed", example = "2023-01-01")
  @Column(nullable = false)
  private LocalDate borrowDate;

  @Schema(description = "Due date for returning the book", example = "2023-01-15")
  @Column(nullable = false)
  private LocalDate dueDate;

  @Schema(description = "Date returned", example = "2023-01-15")
  private LocalDate returnDate;

  @Schema(description = "Status of the loan", example = "BORROWED")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoanStatus status;
}
