import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TransactionApiService } from './transaction-api.service';
import {
  CreateTransactionRequest,
  TransactionPageResponse,
  TransactionResponse,
  TransactionType,
  UpdateTransactionRequest,
} from '../models/transaction.models';

const mockTx: TransactionResponse = {
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
  content: [mockTx],
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

  it('create sends POST to /api/v1/transactions', () => {
    const req: CreateTransactionRequest = {
      walletId: 'wallet-uuid-1',
      type: TransactionType.EXPENSE,
      amount: 50,
      currency: 'USD',
      categoryId: null,
      subCategoryId: null,
      description: 'Groceries',
      transactionDate: '2025-03-01T00:00:00Z',
    };
    service.create(req).subscribe((tx) => expect(tx).toEqual(mockTx));
    const r = httpMock.expectOne('/api/v1/transactions');
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush(mockTx);
  });

  it('getById sends GET to /api/v1/transactions/:id', () => {
    service.getById('tx-uuid-1').subscribe((tx) => expect(tx).toEqual(mockTx));
    httpMock.expectOne('/api/v1/transactions/tx-uuid-1').flush(mockTx);
  });

  it('getAll sends GET to /api/v1/transactions with default params', () => {
    service.getAll(0, 20).subscribe((page) => expect(page).toEqual(mockPage));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/transactions');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('sortBy')).toBe('transactionDate');
    req.flush(mockPage);
  });

  it('getAll passes type and categoryId filters', () => {
    service.getAll(0, 20, { type: TransactionType.EXPENSE, categoryId: 'cat-1' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/transactions');
    expect(req.request.params.get('type')).toBe('EXPENSE');
    expect(req.request.params.get('categoryId')).toBe('cat-1');
    req.flush(mockPage);
  });

  it('getAll passes date range filters', () => {
    service.getAll(0, 20, { from: '2025-01-01T00:00:00Z', to: '2025-03-31T00:00:00Z' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/transactions');
    expect(req.request.params.get('from')).toBe('2025-01-01T00:00:00Z');
    expect(req.request.params.get('to')).toBe('2025-03-31T00:00:00Z');
    req.flush(mockPage);
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

  it('update sends PUT to /api/v1/transactions/:id', () => {
    const req: UpdateTransactionRequest = {
      type: TransactionType.INCOME,
      amount: 100,
      categoryId: null,
      subCategoryId: null,
      description: 'Salary',
      transactionDate: '2025-03-01T00:00:00Z',
    };
    service.update('tx-uuid-1', req).subscribe((tx) => expect(tx).toEqual(mockTx));
    const r = httpMock.expectOne('/api/v1/transactions/tx-uuid-1');
    expect(r.request.method).toBe('PUT');
    expect(r.request.body).toEqual(req);
    r.flush(mockTx);
  });

  it('delete sends DELETE to /api/v1/transactions/:id', () => {
    service.delete('tx-uuid-1').subscribe();
    const r = httpMock.expectOne('/api/v1/transactions/tx-uuid-1');
    expect(r.request.method).toBe('DELETE');
    r.flush(null);
  });
});
