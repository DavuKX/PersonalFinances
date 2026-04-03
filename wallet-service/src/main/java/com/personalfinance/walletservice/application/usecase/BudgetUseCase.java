package com.personalfinance.walletservice.application.usecase;

import com.personalfinance.walletservice.application.dto.BudgetDto;
import com.personalfinance.walletservice.application.dto.BudgetSummaryDto;
import com.personalfinance.walletservice.application.dto.BulkBudgetCommand;
import com.personalfinance.walletservice.application.dto.CreateBudgetCommand;
import com.personalfinance.walletservice.application.dto.UpdateBudgetCommand;

import java.util.List;
import java.util.UUID;

public interface BudgetUseCase {
    BudgetDto create(UUID walletId, UUID userId, CreateBudgetCommand command);
    BudgetDto update(UUID budgetId, UUID userId, UpdateBudgetCommand command);
    void delete(UUID budgetId, UUID userId);
    List<BudgetSummaryDto> listByWallet(UUID walletId, UUID userId);
    BudgetSummaryDto getById(UUID budgetId, UUID userId);
    List<BudgetDto> setBulkBudgets(UUID walletId, UUID userId, BulkBudgetCommand command);
}

