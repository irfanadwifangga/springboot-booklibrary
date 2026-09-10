package com.booklibrary.booklibrary.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.booklibrary.booklibrary.entity.Role;
import com.booklibrary.booklibrary.entity.User;
import com.booklibrary.booklibrary.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end tests that go through the real filter chain (JwtAuthenticationFilter,
 * SecurityConfig, @PreAuthorize) instead of mocked security like the unit tests do.
 * Runs against an in-memory H2 DB (see src/test/resources/application.properties),
 * so it never touches the real Postgres data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // each test method rolls back afterwards, so tests don't leak data into each other
class AuthAndRbacIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @Test
  void register_thenLogin_returnsAccessAndRefreshToken() throws Exception {
    registerUser("alice", "password123");

    MvcResult result = loginRaw("alice", "password123");

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    assertFalse(body.get("token").asText().isBlank());
    assertFalse(body.get("refreshToken").asText().isBlank());
  }

  @Test
  void login_withWrongPassword_returns400() throws Exception {
    registerUser("bob", "password123");

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"bob\",\"password\":\"wrongpass\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void protectedEndpoint_withoutToken_isRejected() throws Exception {
    // No custom AuthenticationEntryPoint is configured, so Spring Security's default
    // for an unauthenticated request here can be 401 or 403 depending on the setup.
    mockMvc.perform(get("/api/books"))
        .andExpect(status().is4xxClientError());
  }

  @Test
  void regularUser_canReadBooks_butCannotCreateCategory() throws Exception {
    registerUser("charlie", "password123");
    String token = loginAndGetToken("charlie", "password123");

    // Read endpoints stay open to any authenticated user
    mockMvc.perform(get("/api/books").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());

    // Write endpoints require ADMIN - a plain USER must be rejected
    mockMvc.perform(post("/api/categories")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Should Fail\",\"description\":\"blocked by RBAC\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminUser_canCreateCategory() throws Exception {
    registerUser("diana", "password123");
    promoteToAdmin("diana");
    String token = loginAndGetToken("diana", "password123"); // fresh token picks up the new role

    mockMvc.perform(post("/api/categories")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Fiction\",\"description\":\"Fictional works\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("Fiction"));
  }

  @Test
  void refreshToken_exchangesForNewAccessToken() throws Exception {
    registerUser("erin", "password123");
    String refreshToken = loginAndGetRefreshToken("erin", "password123");

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty());
  }

  @Test
  void logout_thenRefresh_isRejected() throws Exception {
    registerUser("frank", "password123");

    MvcResult loginResult = loginRaw("frank", "password123");
    JsonNode body = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = body.get("token").asText();
    String refreshToken = body.get("refreshToken").asText();

    mockMvc.perform(post("/api/auth/logout").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    mockMvc.perform(post("/api/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isBadRequest());
  }

  // -- Helpers --

  private void registerUser(String username, String password) throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isCreated());
  }

  private MvcResult loginRaw(String username, String password) throws Exception {
    return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
  }

  private String loginAndGetToken(String username, String password) throws Exception {
    return extractField(loginRaw(username, password), "token");
  }

  private String loginAndGetRefreshToken(String username, String password) throws Exception {
    return extractField(loginRaw(username, password), "refreshToken");
  }

  private String extractField(MvcResult result, String field) throws Exception {
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    String value = body.get(field).asText();
    assertNotNull(value);
    return value;
  }

  private void promoteToAdmin(String username) {
    User user = userRepository.findByUsername(username).orElseThrow();
    user.setRole(Role.ADMIN);
    userRepository.save(user);
  }
}
