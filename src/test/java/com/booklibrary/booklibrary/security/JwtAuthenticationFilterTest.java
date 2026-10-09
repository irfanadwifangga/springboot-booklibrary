package com.booklibrary.booklibrary.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @Mock
  private UserDetails userDetails;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @BeforeEach
  void clearSecurityContextBefore() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void clearSecurityContextAfter() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_whenNoAuthHeader_skipsAuthenticationAndContinuesChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenHeaderIsNotBearer_skipsAuthenticationAndContinuesChain() throws Exception {
    when(request.getHeader("Authorization")).thenReturn("Basic abc123");

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenTokenValid_setsAuthenticationInSecurityContext() throws Exception {
    String token = "valid-token";
    when(request.getHeader("Authorization")).thenReturn("B" + "earer " + token);
    when(jwtUtil.extractUsername(token)).thenReturn("john");
    when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("john");
    when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
    when(jwtUtil.isTokenValid(token, "john")).thenReturn(true);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    assertEquals(userDetails, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenTokenInvalid_doesNotSetAuthentication() throws Exception {
    String token = "expired-token";
    when(request.getHeader("Authorization")).thenReturn("B" + "earer " + token);
    when(jwtUtil.extractUsername(token)).thenReturn("john");
    when(userDetailsService.loadUserByUsername("john")).thenReturn(userDetails);
    when(userDetails.getUsername()).thenReturn("john");
    when(jwtUtil.isTokenValid(token, "john")).thenReturn(false);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenExtractUsernameReturnsNull_skipsUserLookupEntirely() throws Exception {
    // Simulates an invalid/expired token: JwtUtil.extractUsername returns null
    // instead of throwing.
    String token = "garbage-token";
    when(request.getHeader("Authorization")).thenReturn("B" + "earer " + token);
    when(jwtUtil.extractUsername(token)).thenReturn(null);

    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(userDetailsService, never()).loadUserByUsername(any());
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_whenUserNoLongerExists_doesNotPropagateExceptionAndContinuesChain() throws Exception {
    String token = "valid-token";
    when(request.getHeader("Authorization")).thenReturn("B" + "earer " + token);
    when(jwtUtil.extractUsername(token)).thenReturn("ghost");
    when(userDetailsService.loadUserByUsername("ghost"))
        .thenThrow(new UsernameNotFoundException("User not found: ghost"));

    assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));

    assertNull(SecurityContextHolder.getContext().getAuthentication());
    verify(filterChain).doFilter(request, response);
  }
}
