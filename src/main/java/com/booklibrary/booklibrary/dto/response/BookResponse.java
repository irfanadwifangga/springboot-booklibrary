package com.booklibrary.booklibrary.dto.response;

import lombok.Data;

@Data
public class BookResponse {
  private Long id;
  private String title;
  private String author;
  private String isbn;
  private Integer stock;
  private Long categoryId;
}
