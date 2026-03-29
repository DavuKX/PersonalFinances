import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MonthlyAnalyticsResponse } from '../../../core/models/analytics.models';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';

@Component({
  selector: 'app-monthly-kpi-cards',
  imports: [DecimalPipe, SpinnerComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-8"><app-spinner size="lg" /></div>
    } @else if (data()) {
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-6">
        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5">
          <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Income</p>
          <p class="mt-2 text-2xl font-bold text-emerald-600 dark:text-emerald-400">
            {{ data()!.totalIncome | number: '1.2-2' }}
          </p>
          <p class="text-xs text-gray-400 dark:text-gray-500 mt-1">{{ data()!.transactionCount }} transactions</p>
        </div>

        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5">
          <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Expenses</p>
          <p class="mt-2 text-2xl font-bold text-rose-600 dark:text-rose-400">
            {{ data()!.totalExpenses | number: '1.2-2' }}
          </p>
        </div>

        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5">
          <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Net Savings</p>
          <p class="mt-2 text-2xl font-bold"
             [class]="data()!.netSavings >= 0 ? 'text-indigo-600 dark:text-indigo-400' : 'text-rose-600 dark:text-rose-400'">
            {{ data()!.netSavings | number: '1.2-2' }}
          </p>
        </div>

        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5">
          <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Savings Rate</p>
          <p class="mt-2 text-2xl font-bold"
             [class]="data()!.savingsRate >= 0 ? 'text-indigo-600 dark:text-indigo-400' : 'text-rose-600 dark:text-rose-400'">
            {{ data()!.savingsRate | number: '1.1-1' }}%
          </p>
        </div>
      </div>
    } @else {
      <div class="grid grid-cols-2 lg:grid-cols-4 gap-6">
        @for (i of [1,2,3,4]; track i) {
          <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5 h-24 animate-pulse"></div>
        }
      </div>
    }
  `,
})
export class MonthlyKpiCardsComponent {
  readonly data = input<MonthlyAnalyticsResponse | null>(null);
  readonly loading = input(false);
}
