package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.booklibrary.booklibrary.dto.request.LoginRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.repository.UserRepository;
import com.booklibrary.booklibrary.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private JwtUtil jwtUtil;

  @InjectMocks
  private AuthService authService;

  @Test
  void register_whenUsernameAvailable_savesUserWithHashedPassword() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("john");
    request.setPassword("plainPass123");

    when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("plainPass123")).thenReturn("hashedPass");

    authService.register(request);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    assertEquals("john", captor.getValue().getUsername());
    assertEquals("hashedPass", captor.getValue().getPassword());
  }

  @Test
  void register_whenUsernameTaken_throwsBadRequestException() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("john");
    request.setPassword("plainPass123");

    when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

    assertThrows(BadRequestException.class, () -> authService.register(request));
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void login_whenCredentialsValid_returnsToken() {
    LoginRequest request = new LoginRequest();
    request.setUsername("john");
    request.setPassword("plainPass123");

    // authenticationManager.authenticate() left unstubbed -> default mock
    // returns null without throwing, simulating a successful login (the
    // return value isn't used, only the absence of an exception matters).
    when(jwtUtil.generateToken("john")).thenReturn("mocked-jwt-token");

    AuthResponse response = authService.login(request);

    assertEquals("mocked-jwt-token", response.getToken());
  }

  @Test
  void login_whenCredentialsInvalid_throwsBadRequestException() {
    LoginRequest request = new LoginRequest();
    request.setUsername("john");
    request.setPassword("wrongPass");

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThrows(BadRequestException.class, () -> authService.login(request));
    verify(jwtUtil, never()).generateToken(any(String.class));
  }
}
