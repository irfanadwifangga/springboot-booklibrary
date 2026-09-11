package com.booklibrary.booklibrary.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
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
import com.booklibrary.booklibrary.exception.AccountLockedException;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.repository.UserRepository;
import com.booklibrary.booklibrary.security.JwtUtil;

@Service
public class AuthService {

  // Lock the account for LOCK_DURATION_MINUTES after MAX_FAILED_ATTEMPTS wrong passwords in a row.
  private static final int MAX_FAILED_ATTEMPTS = 5;
  private static final long LOCK_DURATION_MINUTES = 15;

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
      // Delegates to CustomUserDetailsService + PasswordEncoder internally. If the account
      // is locked, CustomUserDetailsService already flagged it and this throws LockedException
      // before the password is even checked.
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    } catch (LockedException e) {
      throw new AccountLockedException(
          "Account is locked due to too many failed login attempts. Try again in a few minutes.");
    } catch (AuthenticationException e) {
      // Wrong password (or unknown username) - count it towards the lockout threshold.
      // Same error message for both cases so we don't leak which usernames exist.
      registerFailedAttempt(request.getUsername());
      throw new BadRequestException("Invalid username or password");
    }

    User user = userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BadRequestException("Invalid username or password"));

    resetFailedAttempts(user);

    String accessToken = jwtUtil.generateToken(user.getUsername());
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

    return buildAuthResponse(accessToken, refreshToken.getToken());
  }

  private void registerFailedAttempt(String username) {
    userRepository.findByUsername(username).ifPresent(user -> {
      int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
      user.setFailedLoginAttempts(attempts);
      if (attempts >= MAX_FAILED_ATTEMPTS) {
        user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
      }
      userRepository.save(user);
    });
  }

  private void resetFailedAttempts(User user) {
    boolean hasSomethingToReset = (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0)
        || user.getLockedUntil() != null;
    if (hasSomethingToReset) {
      user.setFailedLoginAttempts(0);
      user.setLockedUntil(null);
      userRepository.save(user);
    }
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
