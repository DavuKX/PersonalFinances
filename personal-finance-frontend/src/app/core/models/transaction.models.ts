import { PageResponse } from './pagination.models';

export enum TransactionType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
}

export interface TransactionResponse {
  id: number;
  walletId: number;
  walletName: string;
  categoryId: number | null;
  categoryName: string | null;
  type: TransactionType;
  amount: number;
  description: string | null;
  date: string;
  createdAt: string;
}

export type TransactionPageResponse = PageResponse<TransactionResponse>;

export interface CreateTransactionRequest {
  walletId: number;
  categoryId: number | null;
  type: TransactionType;
  amount: number;
  description: string | null;
  date: string;
}

export interface UpdateTransactionRequest {
  categoryId: number | null;
  type: TransactionType;
  amount: number;
  description: string | null;
  date: string;
}

