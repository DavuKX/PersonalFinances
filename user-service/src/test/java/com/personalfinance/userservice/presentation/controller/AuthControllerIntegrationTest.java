package com.personalfinance.userservice.presentation.controller;

import tools.jackson.databind.ObjectMapper;
import com.personalfinance.userservice.infrastructure.config.TestTokenBlocklistConfig;
import com.personalfinance.userservice.presentation.request.LoginRequest;
import com.personalfinance.userservice.presentation.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestTokenBlocklistConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private void registerUser() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("alice");
        reg.setEmail("alice@example.com");
        reg.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        registerUser();

        LoginRequest login = new LoginRequest();
        login.setEmail("alice@example.com");
        login.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());
    }

    @Test
    void login_shouldReturn401_whenEmailNotFound() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("unknown@example.com");
        login.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn401_whenPasswordIsWrong() throws Exception {
        registerUser();

        LoginRequest login = new LoginRequest();
        login.setEmail("alice@example.com");
        login.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
