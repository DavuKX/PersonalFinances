package com.personalfinance.userservice.presentation.controller;

import tools.jackson.databind.ObjectMapper;
import com.personalfinance.userservice.infrastructure.config.TestTokenBlocklistConfig;
import com.personalfinance.userservice.infrastructure.persistence.repository.UserJpaRepository;
import com.personalfinance.userservice.presentation.request.LoginRequest;
import com.personalfinance.userservice.presentation.request.RegisterRequest;
import com.personalfinance.userservice.presentation.request.UpdateRolesRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestTokenBlocklistConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserJpaRepository userJpaRepository;

    private String registerAndLoginAsAdmin() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("admin");
        reg.setEmail("admin@example.com");
        reg.setPassword("AdminPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
        userJpaRepository.findByEmail("admin@example.com").ifPresent(entity -> {
            entity.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
            userJpaRepository.save(entity);
        });
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest("admin@example.com", "AdminPass123!"))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String registerRegularUser(String username, String email) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(username);
        reg.setEmail(email);
        reg.setPassword("StrongPass123!");
        MvcResult result = mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    @Test
    void listUsers_shouldReturn200WithPage_whenAdmin() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        registerRegularUser("alice", "alice@example.com");
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void listUsers_shouldReturn403_whenNotAdmin() throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("alice");
        reg.setEmail("alice@example.com");
        reg.setPassword("StrongPass123!");
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest("alice@example.com", "StrongPass123!"))))
                .andExpect(status().isOk())
                .andReturn();
        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_shouldReturn200_whenAdmin() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        String userId = registerRegularUser("alice", "alice@example.com");
        mockMvc.perform(get("/api/v1/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"));
    }

    @Test
    void updateRoles_shouldReturn200_whenAdmin() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        String userId = registerRegularUser("alice", "alice@example.com");
        UpdateRolesRequest rolesReq = new UpdateRolesRequest();
        rolesReq.setRoles(Set.of("ROLE_USER", "ROLE_ADMIN"));
        mockMvc.perform(put("/api/v1/admin/users/" + userId + "/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rolesReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void deleteUser_shouldReturn204_whenAdmin() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        String userId = registerRegularUser("alice", "alice@example.com");
        mockMvc.perform(delete("/api/v1/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturn404_whenUserNotFound() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        mockMvc.perform(delete("/api/v1/admin/users/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUsers_shouldSupportPagination() throws Exception {
        String adminToken = registerAndLoginAsAdmin();
        registerRegularUser("alice", "alice@example.com");
        registerRegularUser("bob", "bob@example.com");
        mockMvc.perform(get("/api/v1/admin/users?page=0&size=2")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content").isArray());
    }
}
