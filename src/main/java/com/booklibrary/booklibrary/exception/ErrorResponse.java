package com.booklibrary.booklibrary.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String message;
  private Map<String, String> errors;
}