package com.personalfinance.walletservice.controller;

import com.personalfinance.walletservice.dto.CreateWalletRequest;
import com.personalfinance.walletservice.dto.UpdateWalletRequest;
import com.personalfinance.walletservice.dto.WalletResponse;
import com.personalfinance.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@RequestHeader("X-User-Id") UUID userId,
                                                       @RequestBody @Valid CreateWalletRequest request) {
        WalletResponse response = walletService.createWallet(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@RequestHeader("X-User-Id") UUID userId,
                                                    @PathVariable UUID id) {
        WalletResponse response = walletService.getWalletById(userId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets(@RequestHeader("X-User-Id") UUID userId) {
        List<WalletResponse> responses = walletService.getWalletsByUser(userId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(@RequestHeader("X-User-Id") UUID userId,
                                                       @PathVariable UUID id,
                                                       @RequestBody @Valid UpdateWalletRequest request) {
        WalletResponse response = walletService.updateWallet(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(@RequestHeader("X-User-Id") UUID userId,
                                             @PathVariable UUID id) {
        walletService.deleteWallet(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
