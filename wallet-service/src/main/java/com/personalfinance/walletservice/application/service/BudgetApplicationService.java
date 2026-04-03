package com.personalfinance.walletservice.application.service;

import com.personalfinance.walletservice.application.dto.BudgetDto;
import com.personalfinance.walletservice.application.dto.BudgetSummaryDto;
import com.personalfinance.walletservice.application.dto.BulkBudgetCommand;
import com.personalfinance.walletservice.application.dto.CreateBudgetCommand;
import com.personalfinance.walletservice.application.dto.UpdateBudgetCommand;
import com.personalfinance.walletservice.application.usecase.BudgetUseCase;
import com.personalfinance.walletservice.domain.exception.BudgetLimitExceededException;
import com.personalfinance.walletservice.domain.exception.BudgetNotFoundException;
import com.personalfinance.walletservice.domain.exception.WalletArchivedStateException;
import com.personalfinance.walletservice.domain.exception.WalletNotFoundException;
import com.personalfinance.walletservice.domain.model.Budget;
import com.personalfinance.walletservice.domain.model.BudgetSpending;
import com.personalfinance.walletservice.domain.model.BudgetType;
import com.personalfinance.walletservice.domain.model.Wallet;
import com.personalfinance.walletservice.domain.port.BudgetRepository;
import com.personalfinance.walletservice.domain.port.BudgetSpendingRepository;
import com.personalfinance.walletservice.domain.port.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BudgetApplicationService implements BudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final BudgetSpendingRepository budgetSpendingRepository;
    private final WalletRepository walletRepository;

    public BudgetApplicationService(BudgetRepository budgetRepository,
                                    BudgetSpendingRepository budgetSpendingRepository,
                                    WalletRepository walletRepository) {
        this.budgetRepository = budgetRepository;
        this.budgetSpendingRepository = budgetSpendingRepository;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public BudgetDto create(UUID walletId, UUID userId, CreateBudgetCommand command) {
        Wallet wallet = loadActiveWallet(walletId, userId);
        validateAmount(command.budgetType(), command.amount());

        budgetRepository.findByWalletIdAndCategoryId(walletId, command.categoryId())
                .ifPresent(b -> { throw new IllegalArgumentException("A budget for this category already exists"); });

        if (command.budgetType() == BudgetType.PERCENTAGE) {
            validatePercentageCap(walletId, userId, command.categoryId(), command.amount());
        }

        Budget budget = Budget.create(walletId, userId, command.categoryId(),
                command.budgetType(), command.amount());
        return toDto(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetDto update(UUID budgetId, UUID userId, UpdateBudgetCommand command) {
        Budget budget = loadBudget(budgetId, userId);
        validateAmount(command.budgetType(), command.amount());

        if (command.budgetType() == BudgetType.PERCENTAGE) {
            validatePercentageCap(budget.getWalletId(), userId, budget.getCategoryId(), command.amount());
        }

        return toDto(budgetRepository.save(budget.withAmount(command.budgetType(), command.amount())));
    }

    @Override
    @Transactional
    public void delete(UUID budgetId, UUID userId) {
        Budget budget = loadBudget(budgetId, userId);
        budgetRepository.deleteById(budget.getId());
    }

    @Override
    public List<BudgetSummaryDto> listByWallet(UUID walletId, UUID userId) {
        Wallet wallet = loadWallet(walletId, userId);
        List<Budget> budgets = budgetRepository.findByWalletIdAndUserId(walletId, userId);
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        List<BudgetSpending> spendings = budgetSpendingRepository.findByWalletIdAndPeriodStart(walletId, periodStart);
        Map<UUID, BigDecimal> spendingByCategoryId = spendings.stream()
                .collect(Collectors.toMap(BudgetSpending::getCategoryId, BudgetSpending::getTotalSpent));
        return budgets.stream()
                .map(b -> toSummaryDto(b, wallet, spendingByCategoryId.getOrDefault(b.getCategoryId(), BigDecimal.ZERO)))
                .toList();
    }

    @Override
    public BudgetSummaryDto getById(UUID budgetId, UUID userId) {
        Budget budget = loadBudget(budgetId, userId);
        Wallet wallet = loadWallet(budget.getWalletId(), userId);
        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        BigDecimal spent = budgetSpendingRepository
                .findByWalletIdAndCategoryIdAndPeriodStart(budget.getWalletId(), budget.getCategoryId(), periodStart)
                .map(BudgetSpending::getTotalSpent)
                .orElse(BigDecimal.ZERO);
        return toSummaryDto(budget, wallet, spent);
    }

    @Override
    @Transactional
    public List<BudgetDto> setBulkBudgets(UUID walletId, UUID userId, BulkBudgetCommand command) {
        Wallet wallet = loadActiveWallet(walletId, userId);

        // Validate total percentage doesn't exceed 100
        BigDecimal totalPct = command.allocations().stream()
                .filter(a -> a.budgetType() == BudgetType.PERCENTAGE)
                .map(BulkBudgetCommand.Allocation::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPct.compareTo(new BigDecimal("100")) > 0) {
            throw new BudgetLimitExceededException("Total percentage allocations exceed 100%");
        }

        // Update wallet monthly income if provided
        if (command.monthlyIncome() != null && command.monthlyIncome().compareTo(BigDecimal.ZERO) > 0) {
            walletRepository.save(wallet.withMonthlyIncome(command.monthlyIncome()));
        }

        // Replace all existing budgets
        budgetRepository.deleteAllByWalletId(walletId);

        List<BudgetDto> result = new ArrayList<>();
        for (BulkBudgetCommand.Allocation alloc : command.allocations()) {
            validateAmount(alloc.budgetType(), alloc.amount());
            Budget budget = Budget.create(walletId, userId, alloc.categoryId(),
                    alloc.budgetType(), alloc.amount());
            result.add(toDto(budgetRepository.save(budget)));
        }
        return result;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Wallet loadWallet(UUID walletId, UUID userId) {
        return walletRepository.findByIdAndUserId(walletId, userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
    }

    private Wallet loadActiveWallet(UUID walletId, UUID userId) {
        Wallet wallet = loadWallet(walletId, userId);
        if (wallet.isArchived()) {
            throw new WalletArchivedStateException("Cannot manage budgets on an archived wallet");
        }
        return wallet;
    }

    private Budget loadBudget(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new BudgetNotFoundException("Budget not found"));
        if (!budget.belongsTo(userId)) {
            throw new BudgetNotFoundException("Budget not found");
        }
        return budget;
    }

    private void validateAmount(BudgetType type, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be positive");
        }
        if (type == BudgetType.PERCENTAGE && amount.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage budget cannot exceed 100%");
        }
    }

    private void validatePercentageCap(UUID walletId, UUID userId, UUID excludedCategoryId, BigDecimal newPct) {
        BigDecimal existingTotal = budgetRepository.findByWalletIdAndUserId(walletId, userId).stream()
                .filter(b -> b.getBudgetType() == BudgetType.PERCENTAGE)
                .filter(b -> !b.getCategoryId().equals(excludedCategoryId))
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (existingTotal.add(newPct).compareTo(new BigDecimal("100")) > 0) {
            throw new BudgetLimitExceededException("Total percentage allocations would exceed 100%");
        }
    }

    private BudgetDto toDto(Budget b) {
        return new BudgetDto(b.getId(), b.getWalletId(), b.getUserId(), b.getCategoryId(),
                b.getBudgetType(), b.getAmount(), b.getPeriod(), b.getCreatedAt(), b.getUpdatedAt());
    }

    private BudgetSummaryDto toSummaryDto(Budget b, Wallet wallet, BigDecimal spent) {
        BigDecimal resolvedAmount;
        if (b.getBudgetType() == BudgetType.FIXED) {
            resolvedAmount = b.getAmount();
        } else {
            BigDecimal income = wallet.getMonthlyIncome();
            resolvedAmount = (income != null && income.compareTo(BigDecimal.ZERO) > 0)
                    ? b.getAmount().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP).multiply(income)
                    : null;
        }
        BigDecimal remaining = resolvedAmount != null ? resolvedAmount.subtract(spent) : null;
        double pctUsed = (resolvedAmount != null && resolvedAmount.compareTo(BigDecimal.ZERO) > 0)
                ? spent.divide(resolvedAmount, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).doubleValue()
                : 0.0;
        return new BudgetSummaryDto(b.getId(), b.getWalletId(), b.getUserId(), b.getCategoryId(),
                b.getBudgetType(), b.getAmount(), resolvedAmount, b.getPeriod(),
                spent, remaining, pctUsed, b.getCreatedAt(), b.getUpdatedAt());
    }
}

