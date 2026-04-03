import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BudgetResponse,
  BudgetSummaryResponse,
  BulkBudgetRequest,
  CreateBudgetRequest,
  UpdateBudgetRequest,
} from '../models/budget.models';

@Injectable({ providedIn: 'root' })
export class BudgetApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/wallets';

  create(walletId: string, request: CreateBudgetRequest): Observable<BudgetResponse> {
    return this.http.post<BudgetResponse>(`${this.baseUrl}/${walletId}/budgets`, request);
  }

  listByWallet(walletId: string): Observable<BudgetSummaryResponse[]> {
    return this.http.get<BudgetSummaryResponse[]>(`${this.baseUrl}/${walletId}/budgets`);
  }

  getById(walletId: string, budgetId: string): Observable<BudgetSummaryResponse> {
    return this.http.get<BudgetSummaryResponse>(`${this.baseUrl}/${walletId}/budgets/${budgetId}`);
  }

  update(walletId: string, budgetId: string, request: UpdateBudgetRequest): Observable<BudgetResponse> {
    return this.http.put<BudgetResponse>(`${this.baseUrl}/${walletId}/budgets/${budgetId}`, request);
  }

  delete(walletId: string, budgetId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${walletId}/budgets/${budgetId}`);
  }

  setBulk(walletId: string, request: BulkBudgetRequest): Observable<BudgetResponse[]> {
    return this.http.put<BudgetResponse[]>(`${this.baseUrl}/${walletId}/budgets/bulk`, request);
  }
}

