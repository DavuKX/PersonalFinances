package com.personalfinance.walletservice.presentation.controller;

import com.personalfinance.walletservice.application.dto.BudgetDto;
import com.personalfinance.walletservice.application.dto.BudgetSummaryDto;
import com.personalfinance.walletservice.application.dto.BulkBudgetCommand;
import com.personalfinance.walletservice.application.dto.CreateBudgetCommand;
import com.personalfinance.walletservice.application.dto.UpdateBudgetCommand;
import com.personalfinance.walletservice.application.usecase.BudgetUseCase;
import com.personalfinance.walletservice.presentation.request.BulkBudgetRequest;
import com.personalfinance.walletservice.presentation.request.CreateBudgetRequest;
import com.personalfinance.walletservice.presentation.request.UpdateBudgetRequest;
import com.personalfinance.walletservice.presentation.response.BudgetResponse;
import com.personalfinance.walletservice.presentation.response.BudgetSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets/{walletId}/budgets")
public class BudgetController {

    private final BudgetUseCase budgetUseCase;

    public BudgetController(BudgetUseCase budgetUseCase) {
        this.budgetUseCase = budgetUseCase;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @RequestBody @Valid CreateBudgetRequest request) {
        BudgetDto dto = budgetUseCase.create(walletId, userId,
                new CreateBudgetCommand(request.getCategoryId(), request.getBudgetType(), request.getAmount()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @GetMapping
    public ResponseEntity<List<BudgetSummaryResponse>> listByWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId) {
        return ResponseEntity.ok(budgetUseCase.listByWallet(walletId, userId)
                .stream().map(this::toSummaryResponse).toList());
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetSummaryResponse> getById(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @PathVariable UUID budgetId) {
        return ResponseEntity.ok(toSummaryResponse(budgetUseCase.getById(budgetId, userId)));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @PathVariable UUID budgetId,
            @RequestBody @Valid UpdateBudgetRequest request) {
        BudgetDto dto = budgetUseCase.update(budgetId, userId,
                new UpdateBudgetCommand(request.getBudgetType(), request.getAmount()));
        return ResponseEntity.ok(toResponse(dto));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @PathVariable UUID budgetId) {
        budgetUseCase.delete(budgetId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bulk")
    public ResponseEntity<List<BudgetResponse>> setBulk(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @RequestBody @Valid BulkBudgetRequest request) {
        List<BulkBudgetCommand.Allocation> allocations = request.getAllocations().stream()
                .map(a -> new BulkBudgetCommand.Allocation(a.getCategoryId(), a.getBudgetType(), a.getAmount()))
                .toList();
        List<BudgetDto> dtos = budgetUseCase.setBulkBudgets(walletId, userId,
                new BulkBudgetCommand(request.getMonthlyIncome(), allocations));
        return ResponseEntity.ok(dtos.stream().map(this::toResponse).toList());
    }

    private BudgetResponse toResponse(BudgetDto dto) {
        BudgetResponse r = new BudgetResponse();
        r.setId(dto.id());
        r.setWalletId(dto.walletId());
        r.setUserId(dto.userId());
        r.setCategoryId(dto.categoryId());
        r.setBudgetType(dto.budgetType());
        r.setAmount(dto.amount());
        r.setPeriod(dto.period());
        r.setCreatedAt(dto.createdAt());
        r.setUpdatedAt(dto.updatedAt());
        return r;
    }

    private BudgetSummaryResponse toSummaryResponse(BudgetSummaryDto dto) {
        BudgetSummaryResponse r = new BudgetSummaryResponse();
        r.setId(dto.id());
        r.setWalletId(dto.walletId());
        r.setUserId(dto.userId());
        r.setCategoryId(dto.categoryId());
        r.setBudgetType(dto.budgetType());
        r.setAmount(dto.amount());
        r.setResolvedAmount(dto.resolvedAmount());
        r.setPeriod(dto.period());
        r.setSpentAmount(dto.spentAmount());
        r.setRemainingAmount(dto.remainingAmount());
        r.setPercentUsed(dto.percentUsed());
        r.setCreatedAt(dto.createdAt());
        r.setUpdatedAt(dto.updatedAt());
        return r;
    }
}

