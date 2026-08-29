package com.booklibrary.booklibrary.dto.response;

import java.time.LocalDate;

import com.booklibrary.booklibrary.entity.LoanStatus;

import lombok.Data;

@Data
public class LoanResponse {
  private Long id;
  private Long bookId;
  private Long memberId;
  private LocalDate loanDate;
  private LocalDate dueDate;
  private LocalDate returnDate;
  private LoanStatus status;
}
