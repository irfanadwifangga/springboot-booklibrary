package com.booklibrary.booklibrary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.booklibrary.booklibrary.entity.RefreshToken;
import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  @InjectMocks
  private RefreshTokenService refreshTokenService;

  @BeforeEach
  void setExpirationMs() throws Exception {
    // @Value can't be injected outside a Spring context - set it directly via
    // reflection so createRefreshToken() has a real duration to work with.
    Field field = RefreshTokenService.class.getDeclaredField("refreshExpirationMs");
    field.setAccessible(true);
    field.set(refreshTokenService, 604800000L);
  }

  @Test
  void createRefreshToken_whenNoExistingToken_savesNewOne() {
    User user = new User();
    user.setId(1L);
    user.setUsername("john");

    when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RefreshToken result = refreshTokenService.createRefreshToken(user);

    assertNotNull(result.getToken());
    assertEquals(user, result.getUser());
    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }

  @Test
  void createRefreshToken_whenExistingToken_deletesOldOneFirst() {
    User user = new User();
    user.setId(1L);
    user.setUsername("john");

    RefreshToken oldToken = new RefreshToken();
    oldToken.setUser(user);
    oldToken.setToken("old-token");

    when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(oldToken));
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RefreshToken result = refreshTokenService.createRefreshToken(user);

    verify(refreshTokenRepository).delete(oldToken);
    assertNotNull(result.getToken());
    assertEquals(false, result.getToken().equals("old-token"));
  }

  @Test
  void verifyExpiration_whenNotExpired_returnsSameToken() {
    RefreshToken token = new RefreshToken();
    token.setExpiryDate(Instant.now().plusSeconds(3600));

    RefreshToken result = refreshTokenService.verifyExpiration(token);

    assertEquals(token, result);
    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }

  @Test
  void verifyExpiration_whenExpired_deletesTokenAndThrows() {
    RefreshToken token = new RefreshToken();
    token.setExpiryDate(Instant.now().minusSeconds(1));

    assertThrows(BadRequestException.class, () -> refreshTokenService.verifyExpiration(token));
    verify(refreshTokenRepository).delete(token);
  }

  @Test
  void deleteByUser_whenTokenExists_deletesIt() {
    User user = new User();
    user.setUsername("john");

    RefreshToken token = new RefreshToken();
    token.setUser(user);

    when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(token));

    refreshTokenService.deleteByUser(user);

    verify(refreshTokenRepository).delete(token);
  }

  @Test
  void deleteByUser_whenNoTokenExists_doesNothing() {
    User user = new User();
    user.setUsername("john");

    when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());

    refreshTokenService.deleteByUser(user);

    verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
  }
}
