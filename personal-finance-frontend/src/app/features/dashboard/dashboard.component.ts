import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { WalletApiService } from '../../core/services/wallet-api.service';
import { TransactionApiService } from '../../core/services/transaction-api.service';
import { AuthStateService } from '../../core/services/auth-state.service';
import { MonthlyAnalyticsResponse, TrendResponse } from '../../core/models/analytics.models';
import { CurrencyTotal, WalletTotalsResponse } from '../../core/models/wallet.models';
import { TransactionResponse } from '../../core/models/transaction.models';
import { MonthlyKpiCardsComponent } from '../analytics/monthly-kpi-cards/monthly-kpi-cards.component';
import { TrendChartComponent } from '../analytics/trend-chart/trend-chart.component';
import { CardComponent } from '../../shared/components/card/card.component';
import { BadgeComponent } from '../../shared/components/badge/badge.component';
import { ButtonComponent } from '../../shared/components/button/button.component';
import { SpinnerComponent } from '../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    MonthlyKpiCardsComponent,
    TrendChartComponent,
    CardComponent,
    BadgeComponent,
    ButtonComponent,
    SpinnerComponent,
    EmptyStateComponent,
  ],
  template: `
    <div class="space-y-8">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
            Welcome back{{ username() ? ', ' + username() : '' }}
          </h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
            {{ currentMonthLabel }}
          </p>
        </div>
        <div class="flex gap-2">
          <a routerLink="/wallets"><app-button variant="secondary" size="sm">+ Wallet</app-button></a>
          <a routerLink="/transactions/new"><app-button variant="primary" size="sm">+ Transaction</app-button></a>
        </div>
      </div>

      @if (loadingTotals()) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          @for (i of [1,2,3,4]; track i) {
            <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5 h-24 animate-pulse"></div>
          }
        </div>
      } @else if (totals().length > 0) {
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          @for (total of totals(); track total.currency) {
            <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5">
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">
                Total Balance · {{ total.currency }}
              </p>
              <p class="mt-2 text-2xl font-bold text-gray-900 dark:text-gray-100">
                {{ total.total | number: '1.2-2' }}
              </p>
            </div>
          }
        </div>
      }

      <app-monthly-kpi-cards class="block" [data]="monthlyData()" [loading]="loadingMonthly()" />

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div class="lg:col-span-2">
          <app-card title="6-Month Trend">
            <div class="h-72">
              <app-trend-chart [data]="trendData()" [loading]="loadingTrend()" [mini]="false" />
            </div>
          </app-card>
        </div>

        <app-card title="Recent Transactions" [noPadding]="true">
          @if (loadingTransactions()) {
            <div class="flex justify-center py-8"><app-spinner /></div>
          } @else if (recentTransactions().length === 0) {
            <app-empty-state
              title="No transactions yet"
              description="Start by adding your first transaction."
              icon="💸"
            />
          } @else {
            <ul class="divide-y divide-gray-100 dark:divide-gray-800">
              @for (tx of recentTransactions(); track tx.id) {
                <li class="flex items-center justify-between px-6 py-3">
                  <div class="min-w-0">
                    <p class="text-sm font-medium text-gray-900 dark:text-gray-100 truncate">
                      {{ tx.categoryName ?? tx.description ?? 'Uncategorized' }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                      {{ tx.transactionDate | date: 'mediumDate' }}
                    </p>
                  </div>
                  <div class="ml-4 flex items-center gap-2 shrink-0">
                    <span
                      [class]="tx.type === 'INCOME'
                        ? 'text-sm font-semibold text-emerald-600 dark:text-emerald-400'
                        : tx.type === 'SAVINGS'
                          ? 'text-sm font-semibold text-amber-600 dark:text-amber-400'
                          : 'text-sm font-semibold text-rose-600 dark:text-rose-400'"
                    >
                      {{ tx.type === 'INCOME' ? '+' : '-' }}{{ tx.amount | number: '1.2-2' }}
                    </span>
                    <app-badge [variant]="tx.type === 'INCOME' ? 'success' : tx.type === 'SAVINGS' ? 'warning' : 'danger'">
                      {{ tx.type === 'INCOME' ? 'IN' : tx.type === 'SAVINGS' ? 'SAV' : 'OUT' }}
                    </app-badge>
                  </div>
                </li>
              }
            </ul>
            <div class="px-6 py-3 border-t border-gray-100 dark:border-gray-800">
              <a routerLink="/transactions" class="text-sm text-indigo-600 dark:text-indigo-400 hover:underline">
                View all transactions →
              </a>
            </div>
          }
        </app-card>
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly walletApi = inject(WalletApiService);
  private readonly transactionApi = inject(TransactionApiService);
  private readonly authState = inject(AuthStateService);

  readonly totals = signal<CurrencyTotal[]>([]);
  readonly monthlyData = signal<MonthlyAnalyticsResponse | null>(null);
  readonly trendData = signal<TrendResponse[]>([]);
  readonly recentTransactions = signal<TransactionResponse[]>([]);

  readonly loadingTotals = signal(false);
  readonly loadingMonthly = signal(false);
  readonly loadingTrend = signal(false);
  readonly loadingTransactions = signal(false);

  readonly username = computed(() => this.authState.currentUser()?.username ?? null);

  readonly currentMonthLabel = new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

  ngOnInit(): void {
    this.loadTotals();
    this.loadMonthly();
    this.loadTrend();
    this.loadRecentTransactions();
  }

  private loadTotals(): void {
    this.loadingTotals.set(true);
    this.walletApi.getTotals().subscribe({
      next: (response: WalletTotalsResponse) => {
        this.totals.set(response.totals);
        this.loadingTotals.set(false);
      },
      error: () => this.loadingTotals.set(false),
    });
  }

  private loadMonthly(): void {
    const now = new Date();
    this.loadingMonthly.set(true);
    this.analyticsApi.getMonthly({ year: now.getFullYear(), month: now.getMonth() + 1 }).subscribe({
      next: (data) => {
        this.monthlyData.set(data);
        this.loadingMonthly.set(false);
      },
      error: () => {
        this.monthlyData.set(null);
        this.loadingMonthly.set(false);
      },
    });
  }

  private loadTrend(): void {
    this.loadingTrend.set(true);
    this.analyticsApi.getTrend(undefined, 6).subscribe({
      next: (data) => {
        this.trendData.set(data);
        this.loadingTrend.set(false);
      },
      error: () => {
        this.trendData.set([]);
        this.loadingTrend.set(false);
      },
    });
  }

  private loadRecentTransactions(): void {
    this.loadingTransactions.set(true);
    this.transactionApi.getAll(0, 5).subscribe({
      next: (page) => {
        this.recentTransactions.set(page.content);
        this.loadingTransactions.set(false);
      },
      error: () => {
        this.recentTransactions.set([]);
        this.loadingTransactions.set(false);
      },
    });
  }
}
