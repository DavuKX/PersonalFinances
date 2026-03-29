import { Component, input } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { AnalyticsOverviewComponent } from './analytics-overview.component';
import { AnalyticsApiService } from '../../../core/services/analytics-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { TrendChartComponent } from '../trend-chart/trend-chart.component';
import { CategoryBreakdownChartComponent } from '../category-breakdown-chart/category-breakdown-chart.component';
import {
  CategoryAnalyticsResponse,
  MonthlyAnalyticsResponse,
  TrendResponse,
  WalletBreakdownResponse,
} from '../../../core/models/analytics.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { TransactionType } from '../../../core/models/transaction.models';

@Component({ selector: 'app-trend-chart', template: '', standalone: true })
class TrendChartStub {
  readonly data = input<any[]>([]);
  readonly loading = input(false);
  readonly mini = input(false);
}

@Component({ selector: 'app-category-breakdown-chart', template: '', standalone: true })
class CategoryBreakdownChartStub {
  readonly data = input<any[]>([]);
  readonly categories = input<any[]>([]);
  readonly loading = input(false);
}

const mockMonthly: MonthlyAnalyticsResponse = {
  year: 2026, month: 3, totalIncome: 3000, totalExpenses: 1500,
  netSavings: 1500, savingsRate: 50, transactionCount: 10,
};

const mockTrend: TrendResponse[] = [
  { year: 2026, month: 1, totalIncome: 2000, totalExpenses: 1000, netSavings: 1000, savingsRate: 50, transactionCount: 5 },
];

const mockCategory: CategoryAnalyticsResponse[] = [
  { categoryId: 'c-1', transactionType: TransactionType.EXPENSE, year: 2026, month: 3, totalAmount: 500, transactionCount: 3 },
];

const mockBreakdown: WalletBreakdownResponse[] = [
  { walletId: 'w-1', year: 2026, month: 3, totalIncome: 3000, totalExpenses: 1500, netSavings: 1500, savingsRate: 50, transactionCount: 10 },
];

const mockWallets: WalletResponse[] = [
  { id: 'w-1', name: 'Main', currency: 'USD', balance: 1000, archived: false, spendingLimitAmount: null, spendingLimitPeriod: null, archivedAt: null, createdAt: '', updatedAt: '' },
];

describe('AnalyticsOverviewComponent', () => {
  let fixture: ComponentFixture<AnalyticsOverviewComponent>;
  let component: AnalyticsOverviewComponent;

  const analyticsApiMock = {
    getMonthly: vi.fn(),
    getByCategory: vi.fn(),
    getTrend: vi.fn(),
    getWalletBreakdown: vi.fn(),
  };

  const walletApiMock = { getAll: vi.fn() };
  const categoryApiMock = { getAll: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    analyticsApiMock.getMonthly.mockReturnValue(of(mockMonthly));
    analyticsApiMock.getByCategory.mockReturnValue(of(mockCategory));
    analyticsApiMock.getTrend.mockReturnValue(of(mockTrend));
    analyticsApiMock.getWalletBreakdown.mockReturnValue(of(mockBreakdown));
    walletApiMock.getAll.mockReturnValue(of(mockWallets));
    categoryApiMock.getAll.mockReturnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        { provide: AnalyticsApiService, useValue: analyticsApiMock },
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: CategoryApiService, useValue: categoryApiMock },
      ],
    });
    TestBed.overrideComponent(AnalyticsOverviewComponent, {
      remove: { imports: [TrendChartComponent, CategoryBreakdownChartComponent] },
      add: { imports: [TrendChartStub, CategoryBreakdownChartStub] },
    });

    fixture = TestBed.createComponent(AnalyticsOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('calls all API methods on init', () => {
    expect(analyticsApiMock.getMonthly).toHaveBeenCalled();
    expect(analyticsApiMock.getByCategory).toHaveBeenCalled();
    expect(analyticsApiMock.getTrend).toHaveBeenCalled();
    expect(analyticsApiMock.getWalletBreakdown).toHaveBeenCalled();
    expect(walletApiMock.getAll).toHaveBeenCalled();
    expect(categoryApiMock.getAll).toHaveBeenCalled();
  });

  it('populates monthlyData signal from API response', () => {
    expect(component.monthlyData()).toEqual(mockMonthly);
  });

  it('populates trendData signal from API response', () => {
    expect(component.trendData()).toEqual(mockTrend);
  });

  it('populates wallets signal from API response', () => {
    expect(component.wallets()).toEqual(mockWallets);
  });

  it('renders the Analytics heading', () => {
    expect(fixture.nativeElement.textContent).toContain('Analytics');
  });

  it('renders month and year selects', () => {
    const selects = fixture.nativeElement.querySelectorAll('select');
    expect(selects.length).toBeGreaterThanOrEqual(3);
  });

  it('sets monthlyData to null on API error', () => {
    analyticsApiMock.getMonthly.mockReturnValue(throwError(() => new Error('fail')));
    component.reload();
    expect(component.monthlyData()).toBeNull();
  });

  it('sets trendData to empty array on API error', () => {
    analyticsApiMock.getTrend.mockReturnValue(throwError(() => new Error('fail')));
    component.reload();
    expect(component.trendData()).toEqual([]);
  });

  it('monthLabel computed reflects selectedMonth', () => {
    component.selectedMonth.set(1);
    expect(component.monthLabel()).toBe('January');
    component.selectedMonth.set(12);
    expect(component.monthLabel()).toBe('December');
  });

  it('years array has 5 entries ending at current year', () => {
    const currentYear = new Date().getFullYear();
    expect(component.years[0]).toBe(currentYear);
    expect(component.years).toHaveLength(5);
  });

  it('reload passes selectedWalletId to getMonthly', () => {
    component.selectedWalletId.set('w-1');
    component.reload();
    const call = analyticsApiMock.getMonthly.mock.calls.at(-1)![0];
    expect(call.walletId).toBe('w-1');
  });
});





