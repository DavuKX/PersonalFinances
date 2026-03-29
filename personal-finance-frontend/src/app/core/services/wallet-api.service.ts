import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CreateWalletRequest,
  SpendingLimitRequest,
  UpdateWalletRequest,
  WalletPageResponse,
  WalletResponse,
  WalletTotalsResponse,
} from '../models/wallet.models';

@Injectable({ providedIn: 'root' })
export class WalletApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/wallets';

  getAll(): Observable<WalletResponse[]> {
    return this.http.get<WalletResponse[]>(this.baseUrl);
  }

  getPaged(
    page: number,
    size: number,
    includeArchived = false,
    sortBy = 'createdAt',
    direction: 'asc' | 'desc' = 'desc',
  ): Observable<WalletPageResponse> {
    return this.http.get<WalletPageResponse>(`${this.baseUrl}/paged`, {
      params: { page, size, includeArchived, sortBy, direction },
    });
  }

  getById(id: string): Observable<WalletResponse> {
    return this.http.get<WalletResponse>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateWalletRequest): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(this.baseUrl, request);
  }

  update(id: string, request: UpdateWalletRequest): Observable<WalletResponse> {
    return this.http.put<WalletResponse>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  setSpendingLimit(id: string, request: SpendingLimitRequest): Observable<WalletResponse> {
    return this.http.put<WalletResponse>(`${this.baseUrl}/${id}/spending-limit`, request);
  }

  removeSpendingLimit(id: string): Observable<WalletResponse> {
    return this.http.delete<WalletResponse>(`${this.baseUrl}/${id}/spending-limit`);
  }

  archive(id: string): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(`${this.baseUrl}/${id}/archive`, {});
  }

  restore(id: string): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(`${this.baseUrl}/${id}/restore`, {});
  }

  getTotals(): Observable<WalletTotalsResponse> {
    return this.http.get<WalletTotalsResponse>(`${this.baseUrl}/totals`);
  }
}
