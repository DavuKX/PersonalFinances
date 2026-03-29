import { PageResponse } from './pagination.models';

export enum LimitPeriod {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
}

export interface WalletResponse {
  id: string;
  name: string;
  currency: string;
  balance: number;
  archived: boolean;
  spendingLimitAmount: number | null;
  spendingLimitPeriod: LimitPeriod | null;
  archivedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export type WalletPageResponse = PageResponse<WalletResponse>;

export interface CreateWalletRequest {
  name: string;
  currency: string;
  balance: number;
}

export interface UpdateWalletRequest {
  name: string;
}

export interface SpendingLimitRequest {
  amount: number;
  period: LimitPeriod;
}

export interface CurrencyTotal {
  currency: string;
  total: number;
}

export interface WalletTotalsResponse {
  totals: CurrencyTotal[];
}
