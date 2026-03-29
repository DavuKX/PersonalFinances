import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { WalletApiService } from './wallet-api.service';
import {
  CreateWalletRequest,
  LimitPeriod,
  SpendingLimitRequest,
  UpdateWalletRequest,
  WalletPageResponse,
  WalletResponse,
  WalletTotalsResponse,
} from '../models/wallet.models';

const mockWallet: WalletResponse = {
  id: 'wallet-uuid-1',
  name: 'My Wallet',
  currency: 'USD',
  balance: 1000,
  archived: false,
  spendingLimitAmount: null,
  spendingLimitPeriod: null,
  archivedAt: null,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

const mockPage: WalletPageResponse = {
  content: [mockWallet],
  page: 0,
  size: 20,
  totalElements: 1,
  totalPages: 1,
};

describe('WalletApiService', () => {
  let service: WalletApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WalletApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll sends GET to /api/v1/wallets', () => {
    service.getAll().subscribe((wallets) => expect(wallets).toEqual([mockWallet]));
    httpMock.expectOne('/api/v1/wallets').flush([mockWallet]);
  });

  it('getPaged sends GET to /api/v1/wallets/paged with default params', () => {
    service.getPaged(0, 20).subscribe((page) => expect(page).toEqual(mockPage));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/wallets/paged');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('20');
    expect(req.request.params.get('includeArchived')).toBe('false');
    req.flush(mockPage);
  });

  it('getPaged includes includeArchived=true when requested', () => {
    service.getPaged(0, 20, true).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/wallets/paged');
    expect(req.request.params.get('includeArchived')).toBe('true');
    req.flush(mockPage);
  });

  it('getById sends GET to /api/v1/wallets/:id', () => {
    service.getById('wallet-uuid-1').subscribe((w) => expect(w).toEqual(mockWallet));
    httpMock.expectOne('/api/v1/wallets/wallet-uuid-1').flush(mockWallet);
  });

  it('create sends POST to /api/v1/wallets', () => {
    const req: CreateWalletRequest = { name: 'My Wallet', currency: 'USD', balance: 1000 };
    service.create(req).subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets');
    expect(r.request.method).toBe('POST');
    expect(r.request.body).toEqual(req);
    r.flush(mockWallet);
  });

  it('update sends PUT to /api/v1/wallets/:id', () => {
    const req: UpdateWalletRequest = { name: 'Updated' };
    service.update('wallet-uuid-1', req).subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1');
    expect(r.request.method).toBe('PUT');
    expect(r.request.body).toEqual(req);
    r.flush(mockWallet);
  });

  it('delete sends DELETE to /api/v1/wallets/:id', () => {
    service.delete('wallet-uuid-1').subscribe();
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1');
    expect(r.request.method).toBe('DELETE');
    r.flush(null);
  });

  it('setSpendingLimit sends PUT to /api/v1/wallets/:id/spending-limit', () => {
    const req: SpendingLimitRequest = { amount: 500, period: LimitPeriod.MONTHLY };
    service.setSpendingLimit('wallet-uuid-1', req).subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1/spending-limit');
    expect(r.request.method).toBe('PUT');
    expect(r.request.body).toEqual(req);
    r.flush(mockWallet);
  });

  it('removeSpendingLimit sends DELETE to /api/v1/wallets/:id/spending-limit', () => {
    service.removeSpendingLimit('wallet-uuid-1').subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1/spending-limit');
    expect(r.request.method).toBe('DELETE');
    r.flush(mockWallet);
  });

  it('archive sends POST to /api/v1/wallets/:id/archive', () => {
    service.archive('wallet-uuid-1').subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1/archive');
    expect(r.request.method).toBe('POST');
    r.flush(mockWallet);
  });

  it('restore sends POST to /api/v1/wallets/:id/restore', () => {
    service.restore('wallet-uuid-1').subscribe((w) => expect(w).toEqual(mockWallet));
    const r = httpMock.expectOne('/api/v1/wallets/wallet-uuid-1/restore');
    expect(r.request.method).toBe('POST');
    r.flush(mockWallet);
  });

  it('getTotals sends GET to /api/v1/wallets/totals', () => {
    const mockTotals: WalletTotalsResponse = { totals: [{ currency: 'USD', total: 1000 }] };
    service.getTotals().subscribe((t) => expect(t).toEqual(mockTotals));
    httpMock.expectOne('/api/v1/wallets/totals').flush(mockTotals);
  });
});

