package com.personalfinance.userservice.controller;

import tools.jackson.databind.ObjectMapper;
import com.personalfinance.userservice.dto.LoginRequest;
import com.personalfinance.userservice.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void login_shouldReturnToken() throws Exception {
        registerUser();

        LoginRequest login = new LoginRequest();
        login.setEmail("alice@example.com");
        login.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_shouldReturn400_whenEmailNotFound() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setEmail("unknown@example.com");
        login.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenPasswordIsWrong() throws Exception {
        registerUser();

        LoginRequest login = new LoginRequest();
        login.setEmail("alice@example.com");
        login.setPassword("WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest());
    }
}
