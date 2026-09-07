package com.booklibrary.booklibrary.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.booklibrary.booklibrary.entity.RefreshToken;
import com.booklibrary.booklibrary.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  Optional<RefreshToken> findByUser(User user);
}
