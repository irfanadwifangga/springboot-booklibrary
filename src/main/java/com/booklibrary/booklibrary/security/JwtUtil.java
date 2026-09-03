package com.booklibrary.booklibrary.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiration-ms}")
  private long expirationMs;

  public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  public String extractUsername(String token) {
    Claims claims = extractAllClaims(token);
    return claims != null ? claims.getSubject() : null;
  }

  public boolean isTokenValid(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return extractedUsername != null
        && extractedUsername.equals(username)
        && !isTokenExpired(token);
  }

  // -- Helper methods for extracting claims and validating tokens --

  private Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (JwtException | IllegalArgumentException e) {
      // Invalid/expired/tampered token -> fail gracefully instead of throwing.
      return null;
    }
  }

  private boolean isTokenExpired(String token) {
    Claims claims = extractAllClaims(token);
    if (claims == null || claims.getExpiration() == null) {
      return true;
    }

    return claims.getExpiration().before(new Date());
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
