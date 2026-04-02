import { Component, input } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { WalletBreakdownResponse } from '../../../core/models/analytics.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-wallet-breakdown-table',
  imports: [DecimalPipe, SpinnerComponent, EmptyStateComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-8"><app-spinner size="lg" /></div>
    } @else if (data().length === 0) {
      <app-empty-state
        title="No wallet data"
        description="No transactions found for this period."
        icon="💳"
      />
    } @else {
      <div class="overflow-x-auto">
        <table class="w-full text-sm text-left">
          <thead>
            <tr class="border-b border-gray-200 dark:border-gray-700">
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400">Wallet</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Income</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Expenses</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Savings</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Net Savings</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Savings Rate</th>
              <th class="pb-3 font-medium text-gray-500 dark:text-gray-400 text-right">Transactions</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
            @for (row of data(); track row.walletId) {
              <tr class="hover:bg-gray-50 dark:hover:bg-gray-800/50">
                <td class="py-3 font-medium text-gray-900 dark:text-gray-100">{{ walletName(row.walletId) }}</td>
                <td class="py-3 text-right text-emerald-600 dark:text-emerald-400">{{ row.totalIncome | number: '1.2-2' }}</td>
                <td class="py-3 text-right text-rose-600 dark:text-rose-400">{{ row.totalExpenses | number: '1.2-2' }}</td>
                <td class="py-3 text-right text-amber-600 dark:text-amber-400">{{ row.totalSavings | number: '1.2-2' }}</td>
                <td
                  class="py-3 text-right"
                  [class]="row.netSavings >= 0 ? 'text-indigo-600 dark:text-indigo-400' : 'text-rose-600 dark:text-rose-400'"
                >
                  {{ row.netSavings | number: '1.2-2' }}
                </td>
                <td
                  class="py-3 text-right"
                  [class]="row.savingsRate >= 0 ? 'text-indigo-600 dark:text-indigo-400' : 'text-rose-600 dark:text-rose-400'"
                >
                  {{ row.savingsRate | number: '1.1-1' }}%
                </td>
                <td class="py-3 text-right text-gray-500 dark:text-gray-400">{{ row.transactionCount }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class WalletBreakdownTableComponent {
  readonly data = input<WalletBreakdownResponse[]>([]);
  readonly wallets = input<WalletResponse[]>([]);
  readonly loading = input(false);

  protected walletName(walletId: string): string {
    return this.wallets().find((w) => w.id === walletId)?.name ?? walletId;
  }
}

