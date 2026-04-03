package com.personalfinance.walletservice.presentation.controller;

import com.personalfinance.walletservice.application.dto.CreateWalletCommand;
import com.personalfinance.walletservice.application.dto.SetSpendingLimitCommand;
import com.personalfinance.walletservice.application.dto.UpdateWalletCommand;
import com.personalfinance.walletservice.application.dto.WalletDto;
import com.personalfinance.walletservice.application.dto.WalletPageDto;
import com.personalfinance.walletservice.application.dto.WalletTotalsDto;
import com.personalfinance.walletservice.application.usecase.WalletUseCase;
import com.personalfinance.walletservice.presentation.request.CreateWalletRequest;
import com.personalfinance.walletservice.presentation.request.SetSpendingLimitRequest;
import com.personalfinance.walletservice.presentation.request.UpdateWalletRequest;
import com.personalfinance.walletservice.presentation.response.WalletPageResponse;
import com.personalfinance.walletservice.presentation.response.WalletResponse;
import com.personalfinance.walletservice.presentation.response.WalletTotalsResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @GetMapping("")
    public ResponseEntity<List<WalletResponse>> getWallets(@RequestHeader("X-User-Id") UUID userId) {
        List<WalletResponse> wallets = walletUseCase.getAllByUser(userId).stream().map(this::toResponse).toList();
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/paged")
    public ResponseEntity<WalletPageResponse> getWalletsPaged(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        WalletPageDto pageDto = walletUseCase.listByUser(userId, includeArchived, PageRequest.of(page, size, sort));
        List<WalletResponse> content = pageDto.content().stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new WalletPageResponse(content, pageDto.page(), pageDto.size(), pageDto.totalElements(), pageDto.totalPages()));
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

    @PutMapping("/{id}/spending-limit")
    public ResponseEntity<WalletResponse> setSpendingLimit(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid SetSpendingLimitRequest request) {
        return ResponseEntity.ok(toResponse(walletUseCase.setSpendingLimit(userId, id,
                new SetSpendingLimitCommand(request.getAmount(), request.getPeriod()))));
    }

    @DeleteMapping("/{id}/spending-limit")
    public ResponseEntity<WalletResponse> removeSpendingLimit(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(walletUseCase.removeSpendingLimit(userId, id)));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<WalletResponse> archive(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(walletUseCase.archive(userId, id)));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<WalletResponse> restore(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(walletUseCase.restore(userId, id)));
    }

    @GetMapping("/totals")
    public ResponseEntity<WalletTotalsResponse> getTotals(@RequestHeader("X-User-Id") UUID userId) {
        WalletTotalsDto dto = walletUseCase.getTotals(userId);
        return ResponseEntity.ok(new WalletTotalsResponse(dto.totals()));
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
        response.setSpendingLimitAmount(dto.spendingLimitAmount());
        response.setSpendingLimitPeriod(dto.spendingLimitPeriod());
        response.setMonthlyIncome(dto.monthlyIncome());
        response.setArchived(dto.archived());
        response.setArchivedAt(dto.archivedAt());
        response.setCreatedAt(dto.createdAt());
        response.setUpdatedAt(dto.updatedAt());
        return response;
    }
}
