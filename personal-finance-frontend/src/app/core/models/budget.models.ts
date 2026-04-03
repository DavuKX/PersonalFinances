export enum BudgetType {
  FIXED = 'FIXED',
  PERCENTAGE = 'PERCENTAGE',
}

export enum BudgetPeriod {
  MONTHLY = 'MONTHLY',
}

export interface BudgetSummaryResponse {
  id: string;
  walletId: string;
  userId: string;
  categoryId: string;
  budgetType: BudgetType;
  amount: number;
  resolvedAmount: number | null;
  period: BudgetPeriod;
  spentAmount: number;
  remainingAmount: number | null;
  percentUsed: number;
  createdAt: string;
  updatedAt: string;
}

export interface BudgetResponse {
  id: string;
  walletId: string;
  userId: string;
  categoryId: string;
  budgetType: BudgetType;
  amount: number;
  period: BudgetPeriod;
  createdAt: string;
  updatedAt: string;
}

export interface CreateBudgetRequest {
  categoryId: string;
  budgetType: BudgetType;
  amount: number;
}

export interface UpdateBudgetRequest {
  budgetType: BudgetType;
  amount: number;
}

export interface BulkAllocation {
  categoryId: string;
  budgetType: BudgetType;
  amount: number;
}

export interface BulkBudgetRequest {
  monthlyIncome: number;
  allocations: BulkAllocation[];
}

