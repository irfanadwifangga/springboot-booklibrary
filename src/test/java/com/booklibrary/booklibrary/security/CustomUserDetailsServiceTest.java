package com.booklibrary.booklibrary.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  @Test
  void loadUserByUsername_whenUserExists_returnsUserDetailsWithSameCredentials() {
    User user = new User();
    user.setId(1L);
    user.setUsername("john");
    user.setPassword("hashedPass");

    when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

    UserDetails result = customUserDetailsService.loadUserByUsername("john");

    assertEquals("john", result.getUsername());
    assertEquals("hashedPass", result.getPassword());
  }

  @Test
  void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
    when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class,
        () -> customUserDetailsService.loadUserByUsername("ghost"));
  }
}
