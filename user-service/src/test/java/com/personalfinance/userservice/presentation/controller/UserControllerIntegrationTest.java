package com.personalfinance.userservice.presentation.controller;

import tools.jackson.databind.ObjectMapper;
import com.personalfinance.userservice.infrastructure.config.TestTokenBlocklistConfig;
import com.personalfinance.userservice.presentation.request.ChangePasswordRequest;
import com.personalfinance.userservice.presentation.request.LoginRequest;
import com.personalfinance.userservice.presentation.request.RegisterRequest;
import com.personalfinance.userservice.presentation.request.UpdateProfileRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestTokenBlocklistConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private String registerAndLogin() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("alice");
        reg.setEmail("alice@example.com");
        reg.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setEmail("alice@example.com");
        login.setPassword("StrongPass123!");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void me_shouldReturnUserProfile_whenAuthenticated() throws Exception {
        String token = registerAndLogin();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void me_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturn401_whenInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_shouldReturn201_withUserData() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("bob");
        reg.setEmail("bob@example.com");
        reg.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void register_shouldReturn409_whenEmailIsDuplicate() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("alice");
        reg.setEmail("alice@example.com");
        reg.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        RegisterRequest duplicate = new RegisterRequest();
        duplicate.setUsername("alice2");
        duplicate.setEmail("alice@example.com");
        duplicate.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateProfile_shouldReturn200_whenOwnerUpdates() throws Exception {
        String token = registerAndLogin();
        String userId = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asText();

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setUsername("aliceupdated");
        req.setEmail("alice.updated@example.com");

        mockMvc.perform(put("/api/v1/users/" + userId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("aliceupdated"))
                .andExpect(jsonPath("$.email").value("alice.updated@example.com"));
    }

    @Test
    void updateProfile_shouldReturn403_whenUpdatingAnotherUser() throws Exception {
        String token = registerAndLogin();

        RegisterRequest reg2 = new RegisterRequest();
        reg2.setUsername("bob");
        reg2.setEmail("bob@example.com");
        reg2.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated());

        String bobId = objectMapper.readTree(
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest() {{
                                    setEmail("bob@example.com");
                                    setPassword("StrongPass123!");
                                }})))
                        .andReturn().getResponse().getContentAsString()
        ).get("accessToken").asText();

        String bobUserId = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + bobId))
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asText();

        UpdateProfileRequest req = new UpdateProfileRequest();
        req.setUsername("hacker");
        req.setEmail("hacker@example.com");

        mockMvc.perform(put("/api/v1/users/" + bobUserId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_shouldReturn204_whenCurrentPasswordIsCorrect() throws Exception {
        String token = registerAndLogin();
        String userId = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asText();

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("StrongPass123!");
        req.setNewPassword("NewStrongPass456!");

        mockMvc.perform(patch("/api/v1/users/" + userId + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_shouldReturn400_whenCurrentPasswordIsWrong() throws Exception {
        String token = registerAndLogin();
        String userId = objectMapper.readTree(
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getContentAsString()
        ).get("id").asText();

        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword("WrongPassword!");
        req.setNewPassword("NewStrongPass456!");

        mockMvc.perform(patch("/api/v1/users/" + userId + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
