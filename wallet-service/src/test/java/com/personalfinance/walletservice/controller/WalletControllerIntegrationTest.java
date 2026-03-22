package com.personalfinance.walletservice.controller;

import tools.jackson.databind.ObjectMapper;
import com.personalfinance.walletservice.dto.CreateWalletRequest;
import com.personalfinance.walletservice.dto.UpdateWalletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WalletControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    private String createWalletAndGetId(String name, String currency, BigDecimal balance) throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName(name);
        request.setCurrency(currency);
        request.setBalance(balance);

        MvcResult result = mockMvc.perform(post("/api/v1/wallets")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void createWallet_shouldReturn201() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setCurrency("USD");
        request.setBalance(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/v1/wallets")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Main Wallet"))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.balance").value(500))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void createWallet_shouldReturn400_whenNameIsBlank() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("");
        request.setCurrency("USD");

        mockMvc.perform(post("/api/v1/wallets")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createWallet_shouldReturn401_whenNoUserIdHeader() throws Exception {
        CreateWalletRequest request = new CreateWalletRequest();
        request.setName("Main Wallet");
        request.setCurrency("USD");

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWallet_shouldReturnWallet() throws Exception {
        String walletId = createWalletAndGetId("Main Wallet", "USD", BigDecimal.valueOf(100));

        mockMvc.perform(get("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Main Wallet"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void getWallet_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/" + UUID.randomUUID())
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWallet_shouldReturn404_whenBelongsToOtherUser() throws Exception {
        String walletId = createWalletAndGetId("Main Wallet", "USD", BigDecimal.valueOf(100));

        // Use a different user's ID
        mockMvc.perform(get("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getWallets_shouldReturnList() throws Exception {
        createWalletAndGetId("Wallet 1", "USD", BigDecimal.ZERO);
        createWalletAndGetId("Wallet 2", "EUR", BigDecimal.valueOf(200));

        mockMvc.perform(get("/api/v1/wallets")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getWallets_shouldReturnEmptyList_whenNoWallets() throws Exception {
        mockMvc.perform(get("/api/v1/wallets")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateWallet_shouldReturnUpdatedWallet() throws Exception {
        String walletId = createWalletAndGetId("Old Name", "USD", BigDecimal.ZERO);

        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("New Name");

        mockMvc.perform(put("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @Test
    void updateWallet_shouldReturn404_whenNotFound() throws Exception {
        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("New Name");

        mockMvc.perform(put("/api/v1/wallets/" + UUID.randomUUID())
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWallet_shouldReturn204() throws Exception {
        String walletId = createWalletAndGetId("To Delete", "USD", BigDecimal.ZERO);

        mockMvc.perform(delete("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteWallet_shouldReturn404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/wallets/" + UUID.randomUUID())
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/health"))
                .andExpect(status().isOk());
    }
}
