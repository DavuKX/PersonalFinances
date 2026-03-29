export interface MonthlyAnalyticsResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
  currency: string;
}

export interface CategoryAnalyticsResponse {
  categoryId: number;
  categoryName: string;
  total: number;
  percentage: number;
  currency: string;
}

export interface SavingsRateResponse {
  year: number;
  month: number;
  savingsRate: number;
}

export interface TrendResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  currency: string;
}

export interface WalletBreakdownResponse {
  walletId: number;
  walletName: string;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  currency: string;
}

