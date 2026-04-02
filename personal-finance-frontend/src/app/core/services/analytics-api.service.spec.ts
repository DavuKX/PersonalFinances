import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AnalyticsApiService } from './analytics-api.service';
import {
  CategoryAnalyticsResponse,
  MonthlyAnalyticsResponse,
  SavingsRateResponse,
  TrendResponse,
  WalletBreakdownResponse,
} from '../models/analytics.models';
import { TransactionType } from '../models/transaction.models';

const mockMonthly: MonthlyAnalyticsResponse = {
  year: 2025,
  month: 3,
  totalIncome: 3000,
  totalExpenses: 1500,
  totalSavings: 500,
  netSavings: 1000,
  savingsRate: 16.67,
  transactionCount: 10,
};

const mockTrend: TrendResponse = {
  year: 2025, month: 1, totalIncome: 3000, totalExpenses: 1500,
  totalSavings: 500, netSavings: 1000, savingsRate: 16.67, transactionCount: 10,
};

describe('AnalyticsApiService', () => {
  let service: AnalyticsApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AnalyticsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getMonthly sends GET to /api/v1/analytics/monthly', () => {
    service.getMonthly({ year: 2025, month: 3 }).subscribe((r) => expect(r).toEqual(mockMonthly));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/monthly');
    expect(req.request.params.get('year')).toBe('2025');
    expect(req.request.params.get('month')).toBe('3');
    req.flush(mockMonthly);
  });

  it('getMonthly with walletId passes walletId param', () => {
    service.getMonthly({ walletId: 'w-1' }).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/monthly');
    expect(req.request.params.get('walletId')).toBe('w-1');
    req.flush(mockMonthly);
  });

  it('getByCategory sends GET to /api/v1/analytics/by-category', () => {
    const mockCats: CategoryAnalyticsResponse[] = [{
      categoryId: 'c1', transactionType: TransactionType.EXPENSE,
      year: 2025, month: 3, totalAmount: 500, transactionCount: 5,
    }];
    service.getByCategory({}, TransactionType.EXPENSE).subscribe((r) => expect(r).toEqual(mockCats));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/by-category');
    expect(req.request.params.get('type')).toBe('EXPENSE');
    req.flush(mockCats);
  });

  it('getSavingsRate sends GET to /api/v1/analytics/savings-rate', () => {
    const mockRate: SavingsRateResponse = {
      year: 2025, month: 3, totalIncome: 3000, totalExpenses: 1500,
      totalSavings: 500, netSavings: 1000, savingsRate: 16.67,
    };
    service.getSavingsRate({ year: 2025, month: 3 }).subscribe((r) => expect(r).toEqual(mockRate));
    httpMock.expectOne((r) => r.url === '/api/v1/analytics/savings-rate').flush(mockRate);
  });

  it('getTrend sends GET to /api/v1/analytics/trend with months param', () => {
    service.getTrend(undefined, 6).subscribe((r) => expect(r).toEqual([mockTrend]));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/trend');
    expect(req.request.params.get('months')).toBe('6');
    req.flush([mockTrend]);
  });

  it('getTrend passes walletId when provided', () => {
    service.getTrend('w-1', 3).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/trend');
    expect(req.request.params.get('walletId')).toBe('w-1');
    req.flush([mockTrend]);
  });

  it('getWalletBreakdown sends GET to /api/v1/analytics/wallet-breakdown', () => {
    const mockBreakdown: WalletBreakdownResponse[] = [{
      walletId: 'w-1', year: 2025, month: 3, totalIncome: 3000, totalExpenses: 1500,
      totalSavings: 500, netSavings: 1000, savingsRate: 16.67, transactionCount: 10,
    }];
    service.getWalletBreakdown(2025, 3).subscribe((r) => expect(r).toEqual(mockBreakdown));
    const req = httpMock.expectOne((r) => r.url === '/api/v1/analytics/wallet-breakdown');
    expect(req.request.params.get('year')).toBe('2025');
    expect(req.request.params.get('month')).toBe('3');
    req.flush(mockBreakdown);
  });
});

