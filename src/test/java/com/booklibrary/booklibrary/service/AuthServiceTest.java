package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
import com.booklibrary.booklibrary.dto.request.RefreshTokenRequest;
import com.booklibrary.booklibrary.dto.request.RegisterRequest;
import com.booklibrary.booklibrary.dto.response.AuthResponse;
import com.booklibrary.booklibrary.entity.RefreshToken;
import com.booklibrary.booklibrary.entity.Role;
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

  @Mock
  private RefreshTokenService refreshTokenService;

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
    assertEquals(Role.USER, captor.getValue().getRole());
  }

  @Test
  void register_whenUsernameTaken_throwsBadRequestException() {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("john");
    request.setPassword("plainPass123");

    when(userRepository.findByUsername("john")).thenReturn(Optional.of(new User()));

    BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));
    assertEquals("Username is already taken", exception.getMessage());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void login_whenCredentialsValid_returnsAccessAndRefreshToken() {
    LoginRequest request = new LoginRequest();
    request.setUsername("john");
    request.setPassword("plainPass123");

    User user = new User();
    user.setId(1L);
    user.setUsername("john");

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken("mocked-refresh-token");

    // authenticationManager.authenticate() left unstubbed -> default mock
    // returns null without throwing, simulating a successful login.
    when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken("john")).thenReturn("mocked-jwt-token");
    when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

    AuthResponse response = authService.login(request);

    assertEquals("mocked-jwt-token", response.getToken());
    assertEquals("mocked-refresh-token", response.getRefreshToken());
  }

  @Test
  void login_whenCredentialsInvalid_throwsBadRequestException() {
    LoginRequest request = new LoginRequest();
    request.setUsername("john");
    request.setPassword("wrongPass");

    when(authenticationManager.authenticate(any(Authentication.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
    assertEquals("Invalid username or password", exception.getMessage());
    verify(jwtUtil, never()).generateToken(any(String.class));
    verify(refreshTokenService, never()).createRefreshToken(any(User.class));
  }

  @Test
  void refreshToken_whenTokenValid_returnsNewAccessTokenWithSameRefreshToken() {
    User user = new User();
    user.setUsername("john");

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setToken("valid-refresh-token");
    refreshToken.setExpiryDate(Instant.now().plusSeconds(3600));

    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("valid-refresh-token");

    when(refreshTokenService.findByToken("valid-refresh-token")).thenReturn(Optional.of(refreshToken));
    when(refreshTokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
    when(jwtUtil.generateToken("john")).thenReturn("new-access-token");

    AuthResponse response = authService.refreshToken(request);

    assertEquals("new-access-token", response.getToken());
    assertEquals("valid-refresh-token", response.getRefreshToken());
  }

  @Test
  void refreshToken_whenTokenNotFound_throwsBadRequestException() {
    RefreshTokenRequest request = new RefreshTokenRequest();
    request.setRefreshToken("unknown-token");

    when(refreshTokenService.findByToken("unknown-token")).thenReturn(Optional.empty());

    BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.refreshToken(request));
    assertEquals("Invalid refresh token", exception.getMessage());
    verify(jwtUtil, never()).generateToken(any(String.class));
  }

  @Test
  void logout_deletesRefreshTokenForUser() {
    User user = new User();
    user.setUsername("john");

    when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

    authService.logout("john");

    verify(refreshTokenService).deleteByUser(user);
  }
}
