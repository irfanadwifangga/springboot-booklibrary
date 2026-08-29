package com.booklibrary.booklibrary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanRequest {
  @NotNull(message = "Book ID is required")
  private Long bookId;

  @NotNull(message = "Member ID is required")
  private Long memberId;
}
