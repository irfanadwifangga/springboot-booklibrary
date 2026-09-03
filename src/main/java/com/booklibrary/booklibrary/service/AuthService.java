package com.booklibrary.booklibrary.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.dto.request.LoginRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
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

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtUtil = jwtUtil;
  }

  public void register(RegisterRequest request) {
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new BadRequestException("Username is already taken");
    }

    User user = new User();
    user.setUsername(request.getUsername());
    user.setPassword(passwordEncoder.encode(request.getPassword())); // always hash, never store raw
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

    String token = jwtUtil.generateToken(request.getUsername());

    AuthResponse response = new AuthResponse();
    response.setToken(token);
    return response;
  }
}
