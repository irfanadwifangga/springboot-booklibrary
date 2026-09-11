package com.booklibrary.booklibrary.security;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    boolean locked = user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPassword())
        // Spring Security's hasRole('ADMIN') checks for authority "ROLE_ADMIN",
        // so the "ROLE_" prefix here is required, not a style choice.
        .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
        // accountLocked(true) makes authenticationManager.authenticate() throw
        // LockedException BEFORE it even checks the password - see AuthService.login().
        .accountLocked(locked)
        .build();
  }
}
