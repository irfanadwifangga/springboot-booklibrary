package com.booklibrary.booklibrary.dto.response;

import lombok.Data;

@Data
public class MemberResponse {
  private Long id;
  private String name;
  private String email;
  private String phoneNumber;
}
