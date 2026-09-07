package com.booklibrary.booklibrary.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.booklibrary.booklibrary.dto.request.LoginRequest;
import com.booklibrary.booklibrary.dto.request.RefreshTokenRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
import com.booklibrary.booklibrary.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Auth Controller", description = "APIs for user registration, login, and token refresh")
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

  @Operation(summary = "Login", description = "Authenticate with username and password, get back an access token + refresh token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful, tokens returned"),
      @ApiResponse(responseCode = "400", description = "Invalid username or password")
  })
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @Operation(summary = "Refresh access token", description = "Exchange a valid refresh token for a new access token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "New access token issued"),
      @ApiResponse(responseCode = "400", description = "Refresh token invalid or expired")
  })
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }

  @Operation(summary = "Logout", description = "Revoke the current user's refresh token. Requires a valid access token.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Logged out successfully")
  })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(Authentication authentication) {
    authService.logout(authentication.getName());
    return ResponseEntity.noContent().build();
  }
}
