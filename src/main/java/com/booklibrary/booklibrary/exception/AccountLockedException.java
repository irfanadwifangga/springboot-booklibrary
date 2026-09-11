package com.booklibrary.booklibrary.exception;

public class AccountLockedException extends RuntimeException {
  public AccountLockedException(String message) {
    super(message);
  }
}
