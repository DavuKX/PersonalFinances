import { PageResponse } from './pagination.models';

export enum LimitPeriod {
  DAILY = 'DAILY',
  WEEKLY = 'WEEKLY',
  MONTHLY = 'MONTHLY',
}

export interface WalletResponse {
  id: number;
  name: string;
  currency: string;
  balance: number;
  archived: boolean;
  spendingLimit: number | null;
  limitPeriod: LimitPeriod | null;
  createdAt: string;
}

export type WalletPageResponse = PageResponse<WalletResponse>;

export interface CreateWalletRequest {
  name: string;
  currency: string;
  initialBalance: number;
}

export interface UpdateWalletRequest {
  name: string;
  currency: string;
}

export interface SpendingLimitRequest {
  amount: number;
  period: LimitPeriod;
}

export interface WalletTotalsResponse {
  totalBalance: number;
  currency: string;
}

