import { Component, input, signal } from '@angular/core';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { TrendChartComponent } from '../analytics/trend-chart/trend-chart.component';
import { MonthlyKpiCardsComponent } from '../analytics/monthly-kpi-cards/monthly-kpi-cards.component';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { WalletApiService } from '../../core/services/wallet-api.service';
import { TransactionApiService } from '../../core/services/transaction-api.service';
import { AuthStateService } from '../../core/services/auth-state.service';
import { MonthlyAnalyticsResponse, TrendResponse } from '../../core/models/analytics.models';
import { WalletTotalsResponse } from '../../core/models/wallet.models';
import { TransactionResponse, TransactionType } from '../../core/models/transaction.models';
import { PageResponse } from '../../core/models/pagination.models';

@Component({ selector: 'app-trend-chart', template: '', standalone: true })
class TrendChartStub {
  readonly data = input<any[]>([]);
  readonly loading = input(false);
  readonly mini = input(false);
}

@Component({ selector: 'app-monthly-kpi-cards', template: '', standalone: true })
class MonthlyKpiCardsStub {
  readonly data = input<any>(null);
  readonly loading = input(false);
}

const mockMonthly: MonthlyAnalyticsResponse = {
  year: 2026, month: 3, totalIncome: 3000, totalExpenses: 1500,
  totalSavings: 500, netSavings: 1000, savingsRate: 16.67, transactionCount: 10,
};

const mockTotals: WalletTotalsResponse = {
  totals: [{ currency: 'USD', total: 5000 }],
};

const mockTrend: TrendResponse[] = [
  { year: 2026, month: 1, totalIncome: 2000, totalExpenses: 1000, totalSavings: 300, netSavings: 700, savingsRate: 15, transactionCount: 5 },
];

const mockTransaction: TransactionResponse = {
  id: 'tx-1',
  walletId: 'w-1',
  type: TransactionType.EXPENSE,
  amount: 50,
  currency: 'USD',
  categoryId: 'cat-1',
  subCategoryId: null,
  categoryName: 'Food',
  subCategoryName: null,
  description: 'Lunch',
  transactionDate: '2026-03-15',
  createdAt: '',
  updatedAt: '',
};

const mockTransactionPage: PageResponse<TransactionResponse> = {
  content: [mockTransaction],
  page: 0,
  size: 5,
  totalElements: 1,
  totalPages: 1,
};

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let component: DashboardComponent;

  const analyticsApiMock = {
    getMonthly: vi.fn(),
    getTrend: vi.fn(),
  };

  const walletApiMock = { getTotals: vi.fn() };
  const transactionApiMock = { getAll: vi.fn() };
  const authStateMock = { currentUser: signal({ id: 1, username: 'alice', email: '', roles: [], createdAt: '' }) };

  beforeEach(() => {
    vi.clearAllMocks();
    analyticsApiMock.getMonthly.mockReturnValue(of(mockMonthly));
    analyticsApiMock.getTrend.mockReturnValue(of(mockTrend));
    walletApiMock.getTotals.mockReturnValue(of(mockTotals));
    transactionApiMock.getAll.mockReturnValue(of(mockTransactionPage));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AnalyticsApiService, useValue: analyticsApiMock },
        { provide: WalletApiService, useValue: walletApiMock },
        { provide: TransactionApiService, useValue: transactionApiMock },
        { provide: AuthStateService, useValue: authStateMock },
      ],
    });
    TestBed.overrideComponent(DashboardComponent, {
      remove: { imports: [TrendChartComponent, MonthlyKpiCardsComponent] },
      add: { imports: [TrendChartStub, MonthlyKpiCardsStub] },
    });

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('calls all API methods on init', () => {
    expect(analyticsApiMock.getMonthly).toHaveBeenCalled();
    expect(analyticsApiMock.getTrend).toHaveBeenCalledWith(undefined, 6);
    expect(walletApiMock.getTotals).toHaveBeenCalled();
    expect(transactionApiMock.getAll).toHaveBeenCalledWith(0, 5);
  });

  it('populates totals from API response', () => {
    expect(component.totals()).toEqual([{ currency: 'USD', total: 5000 }]);
  });

  it('populates monthlyData from API response', () => {
    expect(component.monthlyData()).toEqual(mockMonthly);
  });

  it('populates trendData from API response', () => {
    expect(component.trendData()).toEqual(mockTrend);
  });

  it('populates recentTransactions from API response', () => {
    expect(component.recentTransactions()).toHaveLength(1);
  });

  it('renders welcome message with username', () => {
    expect(fixture.nativeElement.textContent).toContain('alice');
  });

  it('renders total balance card', () => {
    expect(fixture.nativeElement.textContent).toContain('5,000.00');
  });

  it('renders recent transaction category name', () => {
    expect(fixture.nativeElement.textContent).toContain('Food');
  });

  it('sets monthlyData to null on API error', () => {
    analyticsApiMock.getMonthly.mockReturnValue(throwError(() => new Error('fail')));
    component['loadMonthly']();
    expect(component.monthlyData()).toBeNull();
  });

  it('sets trendData to empty array on API error', () => {
    analyticsApiMock.getTrend.mockReturnValue(throwError(() => new Error('fail')));
    component['loadTrend']();
    expect(component.trendData()).toEqual([]);
  });

  it('sets recentTransactions to empty array on error', () => {
    transactionApiMock.getAll.mockReturnValue(throwError(() => new Error('fail')));
    component['loadRecentTransactions']();
    expect(component.recentTransactions()).toEqual([]);
  });

  it('renders quick action links', () => {
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('+ Wallet');
    expect(text).toContain('+ Transaction');
  });
});





