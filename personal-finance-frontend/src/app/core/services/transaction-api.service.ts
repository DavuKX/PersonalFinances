import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateTransactionRequest,
  TransactionFilters,
  TransactionPageResponse,
  TransactionResponse,
  UpdateTransactionRequest,
} from '../models/transaction.models';
import { SpendingSummaryResponse } from '../models/wallet.models';

@Injectable({ providedIn: 'root' })
export class TransactionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1';

  create(request: CreateTransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.baseUrl}/transactions`, request);
  }

  getById(id: string): Observable<TransactionResponse> {
    return this.http.get<TransactionResponse>(`${this.baseUrl}/transactions/${id}`);
  }

  getAll(
    page: number,
    size: number,
    filters: TransactionFilters = {},
    sortBy = 'transactionDate',
    direction: 'asc' | 'desc' = 'desc',
  ): Observable<TransactionPageResponse> {
    const params: Record<string, string | number | boolean> = { page, size, sortBy, direction };
    if (filters.type) params['type'] = filters.type;
    if (filters.categoryId) params['categoryId'] = filters.categoryId;
    if (filters.from) params['from'] = filters.from;
    if (filters.to) params['to'] = filters.to;
    return this.http.get<TransactionPageResponse>(`${this.baseUrl}/transactions`, { params });
  }

  getByWallet(
    walletId: string,
    page: number,
    size: number,
    sortBy = 'transactionDate',
    direction: 'asc' | 'desc' = 'desc',
  ): Observable<TransactionPageResponse> {
    return this.http.get<TransactionPageResponse>(
      `${this.baseUrl}/wallets/${walletId}/transactions`,
      { params: { page, size, sortBy, direction } },
    );
  }

  update(id: string, request: UpdateTransactionRequest): Observable<TransactionResponse> {
    return this.http.put<TransactionResponse>(`${this.baseUrl}/transactions/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/transactions/${id}`);
  }

  getSpendingSummary(walletId: string, from: string, to: string): Observable<SpendingSummaryResponse> {
    return this.http.get<SpendingSummaryResponse>(
      `${this.baseUrl}/wallets/${walletId}/spending-summary`,
      { params: { from, to } },
    );
  }
}
