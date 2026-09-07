package com.booklibrary.booklibrary.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.RefreshToken;
import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.exception.BadRequestException;
import com.booklibrary.booklibrary.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Value("${jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public RefreshToken createRefreshToken(User user) {
    // Replace any existing refresh token for this user (one active session's
    // worth of refresh token at a time - simplest model).
    refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));

    return refreshTokenRepository.save(refreshToken);
  }

  public Optional<RefreshToken> findByToken(String token) {
    return refreshTokenRepository.findByToken(token);
  }

  public RefreshToken verifyExpiration(RefreshToken refreshToken) {
    if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
      refreshTokenRepository.delete(refreshToken);
      throw new BadRequestException("Refresh token has expired, please login again");
    }
    return refreshToken;
  }

  public void deleteByUser(User user) {
    refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
  }
}
