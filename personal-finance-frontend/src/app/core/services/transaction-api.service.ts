import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransactionPageResponse, TransactionResponse } from '../models/transaction.models';

@Injectable({ providedIn: 'root' })
export class TransactionApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1';

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
}
