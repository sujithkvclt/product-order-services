package com.ecommerce.productorder.integration;

import com.ecommerce.productorder.model.dto.response.AuthResponse;
import com.ecommerce.productorder.model.dto.request.LoginRequest;
import com.ecommerce.productorder.model.dto.request.RegisterRequest;
import com.ecommerce.productorder.model.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should register a new user successfully")
    void testRegisterUser() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("Should fail to register user with duplicate username")
    void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest firstRequest = RegisterRequest.builder()
                .username("duplicateuser")
                .email("first@example.com")
                .password("password123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .username("duplicateuser")
                .email("second@example.com")
                .password("password123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("loginuser")
                .email("login@example.com")
                .password("password123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .username("loginuser")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseBody, AuthResponse.class);
        assertNotNull(authResponse.getToken());
    }

    @Test
    @DisplayName("Should fail to login with invalid credentials")
    void testLoginFailure() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("nonexistent")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail registration with invalid email")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("invalid-email")
                .password("password123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should fail registration with short password")
    void testRegisterShortPassword() throws Exception {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("123")
                .role(UserRole.USER.name())
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }
}
