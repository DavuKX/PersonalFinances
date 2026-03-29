import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AnalyticsApiService } from '../../../core/services/analytics-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import {
  CategoryAnalyticsResponse,
  MonthlyAnalyticsResponse,
  TrendResponse,
  WalletBreakdownResponse,
} from '../../../core/models/analytics.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';
import { MonthlyKpiCardsComponent } from '../monthly-kpi-cards/monthly-kpi-cards.component';
import { CategoryBreakdownChartComponent } from '../category-breakdown-chart/category-breakdown-chart.component';
import { TrendChartComponent } from '../trend-chart/trend-chart.component';
import { WalletBreakdownTableComponent } from '../wallet-breakdown-table/wallet-breakdown-table.component';
import { CardComponent } from '../../../shared/components/card/card.component';

const MONTH_LABELS = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December',
];

@Component({
  selector: 'app-analytics-overview',
  imports: [
    FormsModule,
    MonthlyKpiCardsComponent,
    CategoryBreakdownChartComponent,
    TrendChartComponent,
    WalletBreakdownTableComponent,
    CardComponent,
  ],
  template: `
    <div class="space-y-8">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Analytics</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">
            {{ monthLabel() }} {{ selectedYear() }}
          </p>
        </div>

        <div class="flex flex-wrap gap-3 items-end">
          <div class="flex flex-col gap-1">
            <label class="text-xs text-gray-500 dark:text-gray-400">Month</label>
            <select
              [ngModel]="selectedMonth()"
              (ngModelChange)="selectedMonth.set($event); reload()"
              class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              @for (m of months; track m.value) {
                <option [value]="m.value">{{ m.label }}</option>
              }
            </select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs text-gray-500 dark:text-gray-400">Year</label>
            <select
              [ngModel]="selectedYear()"
              (ngModelChange)="selectedYear.set($event); reload()"
              class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              @for (y of years; track y) {
                <option [value]="y">{{ y }}</option>
              }
            </select>
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs text-gray-500 dark:text-gray-400">Wallet</label>
            <select
              [ngModel]="selectedWalletId()"
              (ngModelChange)="selectedWalletId.set($event); reload()"
              class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">All Wallets</option>
              @for (w of wallets(); track w.id) {
                <option [value]="w.id">{{ w.name }}</option>
              }
            </select>
          </div>
        </div>
      </div>

      <app-monthly-kpi-cards class="block" [data]="monthlyData()" [loading]="loadingMonthly()" />

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <app-card title="Spending by Category">
          <div class="h-64">
            <app-category-breakdown-chart
              [data]="categoryData()"
              [categories]="categories()"
              [loading]="loadingCategory()"
            />
          </div>
        </app-card>

        <app-card title="6-Month Trend">
          <div class="h-64">
            <app-trend-chart [data]="trendData()" [loading]="loadingTrend()" />
          </div>
        </app-card>
      </div>

      <app-card title="Wallet Breakdown">
        <app-wallet-breakdown-table
          [data]="walletBreakdownData()"
          [wallets]="wallets()"
          [loading]="loadingBreakdown()"
        />
      </app-card>
    </div>
  `,
})
export class AnalyticsOverviewComponent implements OnInit {
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly walletApi = inject(WalletApiService);
  private readonly categoryApi = inject(CategoryApiService);

  readonly selectedYear = signal(new Date().getFullYear());
  readonly selectedMonth = signal(new Date().getMonth() + 1);
  readonly selectedWalletId = signal('');

  readonly monthlyData = signal<MonthlyAnalyticsResponse | null>(null);
  readonly categoryData = signal<CategoryAnalyticsResponse[]>([]);
  readonly trendData = signal<TrendResponse[]>([]);
  readonly walletBreakdownData = signal<WalletBreakdownResponse[]>([]);
  readonly wallets = signal<WalletResponse[]>([]);
  readonly categories = signal<CategoryResponse[]>([]);

  readonly loadingMonthly = signal(false);
  readonly loadingCategory = signal(false);
  readonly loadingTrend = signal(false);
  readonly loadingBreakdown = signal(false);

  readonly monthLabel = computed(() => MONTH_LABELS[this.selectedMonth() - 1]);

  readonly months = MONTH_LABELS.map((label, i) => ({ value: i + 1, label }));
  readonly years = this.buildYears();

  ngOnInit(): void {
    this.loadWallets();
    this.loadCategories();
    this.reload();
  }

  reload(): void {
    this.loadMonthly();
    this.loadCategoryBreakdown();
    this.loadTrend();
    this.loadWalletBreakdown();
  }

  private loadWallets(): void {
    this.walletApi.getAll().subscribe((wallets) => this.wallets.set(wallets));
  }

  private loadCategories(): void {
    this.categoryApi.getAll().subscribe((cats) => this.categories.set(cats));
  }

  private loadMonthly(): void {
    this.loadingMonthly.set(true);
    this.analyticsApi
      .getMonthly({
        year: this.selectedYear(),
        month: this.selectedMonth(),
        walletId: this.selectedWalletId() || undefined,
      })
      .subscribe({
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

  private loadCategoryBreakdown(): void {
    this.loadingCategory.set(true);
    this.analyticsApi
      .getByCategory(
        {
          year: this.selectedYear(),
          month: this.selectedMonth(),
          walletId: this.selectedWalletId() || undefined,
        },
        TransactionType.EXPENSE,
      )
      .subscribe({
        next: (data) => {
          this.categoryData.set(data);
          this.loadingCategory.set(false);
        },
        error: () => {
          this.categoryData.set([]);
          this.loadingCategory.set(false);
        },
      });
  }

  private loadTrend(): void {
    this.loadingTrend.set(true);
    this.analyticsApi.getTrend(this.selectedWalletId() || undefined, 6).subscribe({
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

  private loadWalletBreakdown(): void {
    this.loadingBreakdown.set(true);
    this.analyticsApi.getWalletBreakdown(this.selectedYear(), this.selectedMonth()).subscribe({
      next: (data) => {
        this.walletBreakdownData.set(data);
        this.loadingBreakdown.set(false);
      },
      error: () => {
        this.walletBreakdownData.set([]);
        this.loadingBreakdown.set(false);
      },
    });
  }

  private buildYears(): number[] {
    const current = new Date().getFullYear();
    return Array.from({ length: 5 }, (_, i) => current - i);
  }
}


