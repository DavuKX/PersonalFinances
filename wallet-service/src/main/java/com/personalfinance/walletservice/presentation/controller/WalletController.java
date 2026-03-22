package com.personalfinance.walletservice.presentation.controller;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.presentation.request.CreateWalletRequest;
import com.personalfinance.walletservice.presentation.request.UpdateWalletRequest;
import com.personalfinance.walletservice.presentation.response.WalletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletUseCase walletUseCase;

    public WalletController(WalletUseCase walletUseCase) {
        this.walletUseCase = walletUseCase;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateWalletRequest request) {
        WalletDto dto = walletUseCase.create(userId, new CreateWalletCommand(request.getName(), request.getCurrency(), request.getBalance()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(walletUseCase.getById(userId, id)));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getWallets(@RequestHeader("X-User-Id") UUID userId) {
        List<WalletResponse> responses = walletUseCase.getAllByUser(userId).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponse> updateWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateWalletRequest request) {
        return ResponseEntity.ok(toResponse(walletUseCase.update(userId, id, new UpdateWalletCommand(request.getName()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        walletUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    private WalletResponse toResponse(WalletDto dto) {
        WalletResponse response = new WalletResponse();
        response.setId(dto.id());
        response.setUserId(dto.userId());
        response.setName(dto.name());
        response.setCurrency(dto.currency());
        response.setBalance(dto.balance());
        response.setCreatedAt(dto.createdAt());
        response.setUpdatedAt(dto.updatedAt());
        return response;
    }
}
