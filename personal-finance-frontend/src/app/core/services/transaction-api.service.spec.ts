import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TransactionApiService } from './transaction-api.service';
import { TransactionPageResponse, TransactionResponse, TransactionType } from '../models/transaction.models';

const mockTransaction: TransactionResponse = {
  id: 'tx-uuid-1',
  walletId: 'wallet-uuid-1',
  type: TransactionType.EXPENSE,
  amount: 50,
  currency: 'USD',
  categoryId: null,
  subCategoryId: null,
  categoryName: null,
  subCategoryName: null,
  description: 'Groceries',
  transactionDate: '2025-03-01T10:00:00Z',
  createdAt: '2025-03-01T10:00:00Z',
  updatedAt: '2025-03-01T10:00:00Z',
};

const mockPage: TransactionPageResponse = {
  content: [mockTransaction],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

describe('TransactionApiService', () => {
  let service: TransactionApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TransactionApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getByWallet sends GET to /api/v1/wallets/:id/transactions', () => {
    service.getByWallet('wallet-uuid-1', 0, 20).subscribe((page) => expect(page).toEqual(mockPage));
    const req = httpMock.expectOne(
      (r) => r.url === '/api/v1/wallets/wallet-uuid-1/transactions',
    );
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('sortBy')).toBe('transactionDate');
    expect(req.request.params.get('direction')).toBe('desc');
    req.flush(mockPage);
  });

  it('getByWallet passes custom sort params', () => {
    service.getByWallet('wallet-uuid-1', 1, 10, 'amount', 'asc').subscribe();
    const req = httpMock.expectOne(
      (r) => r.url === '/api/v1/wallets/wallet-uuid-1/transactions',
    );
    expect(req.request.params.get('sortBy')).toBe('amount');
    expect(req.request.params.get('direction')).toBe('asc');
    req.flush(mockPage);
  });
});

