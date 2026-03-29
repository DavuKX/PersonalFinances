import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AnalyticsParams,
  CategoryAnalyticsResponse,
  MonthlyAnalyticsResponse,
  SavingsRateResponse,
  TrendResponse,
  WalletBreakdownResponse,
} from '../models/analytics.models';
import { TransactionType } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class AnalyticsApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/analytics';

  getMonthly(params: AnalyticsParams = {}): Observable<MonthlyAnalyticsResponse> {
    return this.http.get<MonthlyAnalyticsResponse>(`${this.baseUrl}/monthly`, {
      params: this.buildParams(params),
    });
  }

  getByCategory(
    params: AnalyticsParams = {},
    type?: TransactionType,
  ): Observable<CategoryAnalyticsResponse[]> {
    const p = this.buildParams(params);
    if (type) p['type'] = type;
    return this.http.get<CategoryAnalyticsResponse[]>(`${this.baseUrl}/by-category`, {
      params: p,
    });
  }

  getSavingsRate(params: AnalyticsParams = {}): Observable<SavingsRateResponse> {
    return this.http.get<SavingsRateResponse>(`${this.baseUrl}/savings-rate`, {
      params: this.buildParams(params),
    });
  }

  getTrend(walletId?: string, months = 6): Observable<TrendResponse[]> {
    const params: Record<string, string | number> = { months };
    if (walletId) params['walletId'] = walletId;
    return this.http.get<TrendResponse[]>(`${this.baseUrl}/trend`, { params });
  }

  getWalletBreakdown(year?: number, month?: number): Observable<WalletBreakdownResponse[]> {
    const params: Record<string, number> = {};
    if (year !== undefined) params['year'] = year;
    if (month !== undefined) params['month'] = month;
    return this.http.get<WalletBreakdownResponse[]>(`${this.baseUrl}/wallet-breakdown`, {
      params,
    });
  }

  private buildParams(p: AnalyticsParams): Record<string, string | number> {
    const params: Record<string, string | number> = {};
    if (p.year !== undefined) params['year'] = p.year;
    if (p.month !== undefined) params['month'] = p.month;
    if (p.walletId) params['walletId'] = p.walletId;
    return params;
  }
}

