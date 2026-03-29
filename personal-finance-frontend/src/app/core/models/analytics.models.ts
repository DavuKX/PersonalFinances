import { TransactionType } from './transaction.models';

export interface MonthlyAnalyticsResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
  transactionCount: number;
}

export interface CategoryAnalyticsResponse {
  categoryId: string;
  transactionType: TransactionType;
  year: number;
  month: number;
  totalAmount: number;
  transactionCount: number;
}

export interface SavingsRateResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
}

export interface TrendResponse {
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
  transactionCount: number;
}

export interface WalletBreakdownResponse {
  walletId: string;
  year: number;
  month: number;
  totalIncome: number;
  totalExpenses: number;
  netSavings: number;
  savingsRate: number;
  transactionCount: number;
}

export interface AnalyticsParams {
  year?: number;
  month?: number;
  walletId?: string;
}
