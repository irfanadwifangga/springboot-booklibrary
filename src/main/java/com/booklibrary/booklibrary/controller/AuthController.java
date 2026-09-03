package com.booklibrary.booklibrary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.dto.request.LoginRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
import com.booklibrary.booklibrary.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth Controller", description = "APIs for user registration and login")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Register a new user", description = "Create a new user account")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User registered successfully"),
      @ApiResponse(responseCode = "400", description = "Username already taken or invalid data")
  })
  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
    authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Operation(summary = "Login", description = "Authenticate with username and password, get back a JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful, token returned"),
      @ApiResponse(responseCode = "400", description = "Invalid username or password")
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }
}
