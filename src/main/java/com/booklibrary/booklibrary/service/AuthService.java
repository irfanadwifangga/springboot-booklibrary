package com.booklibrary.booklibrary.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.LoginRequest;
import com.booklibrary.booklibrary.dto.request.RefreshTokenRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
import com.booklibrary.booklibrary.entity.RefreshToken;
import com.booklibrary.booklibrary.entity.Role;
import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.repository.UserRepository;
import com.booklibrary.booklibrary.security.JwtUtil;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
  }

  public void register(RegisterRequest request) {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new BadRequestException("Username is already taken");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // always hash, never store raw
    // Never let a client pick their own role via the public /register endpoint -
    // everyone starts as USER. Promote to ADMIN manually in the DB if needed.
    user.setRole(Role.USER);
    userRepository.save(user);
  }

  public AuthResponse login(LoginRequest request) {
    try {
      // Delegates to CustomUserDetailsService + PasswordEncoder internally.
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    } catch (AuthenticationException e) {
      // Same message for both cases so we don't leak which usernames exist.
      throw new BadRequestException("Invalid username or password");
    }

    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BadRequestException("Invalid username or password"));

    String accessToken = jwtUtil.generateToken(user.getUsername());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return buildAuthResponse(accessToken, refreshToken.getToken());
  }

  public AuthResponse refreshToken(RefreshTokenRequest request) {
    RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
        .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

    refreshTokenService.verifyExpiration(refreshToken);

    String newAccessToken = jwtUtil.generateToken(refreshToken.getUser().getUsername());

    // No rotation for simplicity: the same refresh token stays valid until its
    // own expiry. A production system would typically rotate it here too.
    return buildAuthResponse(newAccessToken, refreshToken.getToken());
  }

  public void logout(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new BadRequestException("User not found"));
    refreshTokenService.deleteByUser(user);
  }

  private AuthResponse buildAuthResponse(String accessToken, String refreshTokenValue) {
    AuthResponse response = new AuthResponse();
    response.setToken(accessToken);
    response.setRefreshToken(refreshTokenValue);
    return response;
  }
}
