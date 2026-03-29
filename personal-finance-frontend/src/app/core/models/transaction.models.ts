import { PageResponse } from './pagination.models';

export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
}

export interface TransactionResponse {
  id: string;
  walletId: string;
  type: TransactionType;
  amount: number;
  currency: string;
  categoryId: string | null;
  subCategoryId: string | null;
  categoryName: string | null;
  subCategoryName: string | null;
  description: string | null;
  transactionDate: string;
  createdAt: string;
  updatedAt: string;
}

export type TransactionPageResponse = PageResponse<TransactionResponse>;

export interface CreateTransactionRequest {
  walletId: string;
  type: TransactionType;
  amount: number;
  currency: string;
  categoryId: string | null;
  subCategoryId: string | null;
  description: string | null;
  transactionDate: string;
}

export interface UpdateTransactionRequest {
  type: TransactionType;
  amount: number;
  categoryId: string | null;
  subCategoryId: string | null;
  description: string | null;
  transactionDate: string;
}

export interface TransactionFilters {
  type?: TransactionType;
  categoryId?: string;
  from?: string;
  to?: string;
}
