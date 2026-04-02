package com.davukx.analyticsservice.presentation.controller;

import com.davukx.analyticsservice.application.dto.*;
import com.davukx.analyticsservice.application.usecase.AnalyticsUseCase;
import com.davukx.analyticsservice.domain.model.TransactionType;
import com.davukx.analyticsservice.presentation.response.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsUseCase analyticsUseCase;

    public AnalyticsController(AnalyticsUseCase analyticsUseCase) {
        this.analyticsUseCase = analyticsUseCase;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyAnalyticsResponse> getMonthly(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();
        MonthlyAnalyticsDto dto = analyticsUseCase.getMonthly(userId, walletId, resolvedYear, resolvedMonth);
        return ResponseEntity.ok(toMonthlyResponse(dto));
    }

    @GetMapping("/by-category")
    public ResponseEntity<List<CategoryAnalyticsResponse>> getByCategory(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) TransactionType type) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();
        List<CategoryAnalyticsDto> dtos = analyticsUseCase.getByCategory(userId, walletId, resolvedYear, resolvedMonth, type);
        return ResponseEntity.ok(dtos.stream().map(this::toCategoryResponse).toList());
    }

    @GetMapping("/savings-rate")
    public ResponseEntity<SavingsRateResponse> getSavingsRate(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();
        SavingsRateDto dto = analyticsUseCase.getSavingsRate(userId, walletId, resolvedYear, resolvedMonth);
        return ResponseEntity.ok(toSavingsRateResponse(dto));
    }

    @GetMapping("/trend")
    public ResponseEntity<List<TrendResponse>> getTrend(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) UUID walletId,
            @RequestParam(defaultValue = "6") int months) {
        List<TrendPointDto> dtos = analyticsUseCase.getTrend(userId, walletId, months);
        return ResponseEntity.ok(dtos.stream().map(this::toTrendResponse).toList());
    }

    @GetMapping("/wallet-breakdown")
    public ResponseEntity<List<WalletBreakdownResponse>> getWalletBreakdown(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        YearMonth now = YearMonth.now();
        int resolvedYear = year != null ? year : now.getYear();
        int resolvedMonth = month != null ? month : now.getMonthValue();
        List<WalletBreakdownDto> dtos = analyticsUseCase.getWalletBreakdown(userId, resolvedYear, resolvedMonth);
        return ResponseEntity.ok(dtos.stream().map(this::toWalletBreakdownResponse).toList());
    }

    private MonthlyAnalyticsResponse toMonthlyResponse(MonthlyAnalyticsDto dto) {
        MonthlyAnalyticsResponse r = new MonthlyAnalyticsResponse();
        r.setUserId(dto.userId());
        r.setWalletId(dto.walletId());
        r.setYear(dto.year());
        r.setMonth(dto.month());
        r.setTotalIncome(dto.totalIncome());
        r.setTotalExpenses(dto.totalExpenses());
        r.setTotalSavings(dto.totalSavings());
        r.setNetSavings(dto.netSavings());
        r.setSavingsRate(dto.savingsRate());
        r.setTransactionCount(dto.transactionCount());
        return r;
    }

    private CategoryAnalyticsResponse toCategoryResponse(CategoryAnalyticsDto dto) {
        CategoryAnalyticsResponse r = new CategoryAnalyticsResponse();
        r.setCategoryId(dto.categoryId());
        r.setTransactionType(dto.transactionType());
        r.setYear(dto.year());
        r.setMonth(dto.month());
        r.setTotalAmount(dto.totalAmount());
        r.setTransactionCount(dto.transactionCount());
        return r;
    }

    private SavingsRateResponse toSavingsRateResponse(SavingsRateDto dto) {
        SavingsRateResponse r = new SavingsRateResponse();
        r.setUserId(dto.userId());
        r.setWalletId(dto.walletId());
        r.setYear(dto.year());
        r.setMonth(dto.month());
        r.setTotalIncome(dto.totalIncome());
        r.setTotalExpenses(dto.totalExpenses());
        r.setTotalSavings(dto.totalSavings());
        r.setNetSavings(dto.netSavings());
        r.setSavingsRate(dto.savingsRate());
        return r;
    }

    private TrendResponse toTrendResponse(TrendPointDto dto) {
        TrendResponse r = new TrendResponse();
        r.setYear(dto.year());
        r.setMonth(dto.month());
        r.setTotalIncome(dto.totalIncome());
        r.setTotalExpenses(dto.totalExpenses());
        r.setTotalSavings(dto.totalSavings());
        r.setNetSavings(dto.netSavings());
        r.setSavingsRate(dto.savingsRate());
        r.setTransactionCount(dto.transactionCount());
        return r;
    }

    private WalletBreakdownResponse toWalletBreakdownResponse(WalletBreakdownDto dto) {
        WalletBreakdownResponse r = new WalletBreakdownResponse();
        r.setWalletId(dto.walletId());
        r.setYear(dto.year());
        r.setMonth(dto.month());
        r.setTotalIncome(dto.totalIncome());
        r.setTotalExpenses(dto.totalExpenses());
        r.setTotalSavings(dto.totalSavings());
        r.setNetSavings(dto.netSavings());
        r.setSavingsRate(dto.savingsRate());
        r.setTransactionCount(dto.transactionCount());
        return r;
    }
}
