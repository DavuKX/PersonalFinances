package com.personalfinance.walletservice.presentation.controller;
import tools.jackson.databind.ObjectMapper;
import com.personalfinance.walletservice.domain.model.LimitPeriod;
import com.personalfinance.walletservice.presentation.request.CreateWalletRequest;
import com.personalfinance.walletservice.presentation.request.SetSpendingLimitRequest;
import com.personalfinance.walletservice.presentation.request.UpdateWalletRequest;
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
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
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
                .andExpect(jsonPath("$.archived").value(false))
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
                .andExpect(jsonPath("$.name").value("Main Wallet"));
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
    void getWalletsPaged_shouldReturnPagedResults() throws Exception {
        createWalletAndGetId("Wallet 1", "USD", BigDecimal.ZERO);
        createWalletAndGetId("Wallet 2", "EUR", BigDecimal.valueOf(200));
        mockMvc.perform(get("/api/v1/wallets/paged?page=0&size=10")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0));
    }
    @Test
    void getWalletsPaged_shouldExcludeArchived_byDefault() throws Exception {
        String archiveId = createWalletAndGetId("To Archive", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + archiveId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
        createWalletAndGetId("Active", "USD", BigDecimal.ZERO);
        mockMvc.perform(get("/api/v1/wallets/paged?includeArchived=false")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
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
    void updateWallet_shouldReturn409_whenWalletIsArchived() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
        UpdateWalletRequest request = new UpdateWalletRequest();
        request.setName("New Name");
        mockMvc.perform(put("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    @Test
    void setSpendingLimit_shouldReturn200_withLimitData() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.valueOf(1000));
        SetSpendingLimitRequest request = new SetSpendingLimitRequest();
        request.setAmount(BigDecimal.valueOf(500));
        request.setPeriod(LimitPeriod.MONTHLY);
        mockMvc.perform(put("/api/v1/wallets/" + walletId + "/spending-limit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spendingLimitAmount").value(500))
                .andExpect(jsonPath("$.spendingLimitPeriod").value("MONTHLY"));
    }
    @Test
    void setSpendingLimit_shouldReturn400_whenAmountIsZero() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.valueOf(1000));
        SetSpendingLimitRequest request = new SetSpendingLimitRequest();
        request.setAmount(BigDecimal.ZERO);
        request.setPeriod(LimitPeriod.DAILY);
        mockMvc.perform(put("/api/v1/wallets/" + walletId + "/spending-limit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void removeSpendingLimit_shouldReturn200_withNullLimit() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.valueOf(1000));
        SetSpendingLimitRequest limit = new SetSpendingLimitRequest();
        limit.setAmount(BigDecimal.valueOf(300));
        limit.setPeriod(LimitPeriod.WEEKLY);
        mockMvc.perform(put("/api/v1/wallets/" + walletId + "/spending-limit")
                        .header("X-User-Id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(limit)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/v1/wallets/" + walletId + "/spending-limit")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spendingLimitAmount").doesNotExist());
    }
    @Test
    void archive_shouldReturn200_withArchivedTrue() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(true))
                .andExpect(jsonPath("$.archivedAt").isNotEmpty());
    }
    @Test
    void archive_shouldReturn409_whenAlreadyArchived() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isConflict());
    }
    @Test
    void restore_shouldReturn200_withArchivedFalse() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/restore")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archived").value(false));
    }
    @Test
    void restore_shouldReturn409_whenNotArchived() throws Exception {
        String walletId = createWalletAndGetId("My Wallet", "USD", BigDecimal.ZERO);
        mockMvc.perform(post("/api/v1/wallets/" + walletId + "/restore")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isConflict());
    }
    @Test
    void getTotals_shouldReturnTotalsByActiveCurrencies() throws Exception {
        createWalletAndGetId("USD 1", "USD", BigDecimal.valueOf(100));
        createWalletAndGetId("USD 2", "USD", BigDecimal.valueOf(200));
        createWalletAndGetId("EUR 1", "EUR", BigDecimal.valueOf(50));
        String archiveId = createWalletAndGetId("Archived", "USD", BigDecimal.valueOf(9999));
        mockMvc.perform(post("/api/v1/wallets/" + archiveId + "/archive")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/wallets/totals")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals").isArray())
                .andExpect(jsonPath("$.totals.length()").value(2));
    }
    @Test
    void deleteWallet_shouldReturn204() throws Exception {
        String walletId = createWalletAndGetId("To Delete", "USD", BigDecimal.ZERO);
        mockMvc.perform(delete("/api/v1/wallets/" + walletId)
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isNoContent());
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
