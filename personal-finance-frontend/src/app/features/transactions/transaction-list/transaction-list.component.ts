import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import {
  TransactionFilters,
  TransactionResponse,
  TransactionType,
} from '../../../core/models/transaction.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-transaction-list',
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    SpinnerComponent,
    EmptyStateComponent,
    ButtonComponent,
    BadgeComponent,
    PaginationComponent,
    ConfirmationDialogComponent,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Transactions</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ totalElements() }} total</p>
        </div>
        <a routerLink="/transactions/new">
          <app-button variant="primary">+ New Transaction</app-button>
        </a>
      </div>

      <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-4">
        <div class="flex flex-wrap gap-3 items-end">
          <div class="flex gap-1">
            @for (opt of typeOptions; track opt.value) {
              <button
                type="button"
                (click)="setTypeFilter(opt.value)"
                [class]="filterType() === opt.value ? activeFilterBtnClass : inactiveFilterBtnClass"
              >
                {{ opt.label }}
              </button>
            }
          </div>

          <div class="flex flex-col gap-1">
            <label class="text-xs text-gray-500 dark:text-gray-400">Wallet</label>
            <select
              [value]="filterWalletId()"
              (change)="setWalletFilter($any($event.target).value)"
              class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">All Wallets</option>
              @for (w of wallets(); track w.id) {
                <option [value]="w.id">{{ w.name }}</option>
              }
            </select>
          </div>

          @if (!filterWalletId()) {
            <div class="flex flex-col gap-1">
              <label class="text-xs text-gray-500 dark:text-gray-400">Category</label>
              <select
                [value]="filterCategoryId()"
                (change)="setCategoryFilter($any($event.target).value)"
                class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                <option value="">All Categories</option>
                @for (c of topLevelCategories(); track c.id) {
                  <option [value]="c.id">{{ c.name }}</option>
                }
              </select>
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs text-gray-500 dark:text-gray-400">From</label>
              <input
                type="date"
                [value]="filterFrom()"
                (change)="filterFrom.set($any($event.target).value); reload()"
                class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div class="flex flex-col gap-1">
              <label class="text-xs text-gray-500 dark:text-gray-400">To</label>
              <input
                type="date"
                [value]="filterTo()"
                (change)="filterTo.set($any($event.target).value); reload()"
                class="text-sm px-3 py-1.5 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-200 focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
          }

          @if (hasActiveFilters()) {
            <app-button size="sm" variant="ghost" (click)="clearFilters()">Clear Filters</app-button>
          }
        </div>
      </div>

      @if (loading()) {
        <div class="flex justify-center py-12">
          <app-spinner size="lg" />
        </div>
      } @else if (transactions().length === 0) {
        <app-empty-state
          title="No transactions found"
          description="Try adjusting your filters or create a new transaction."
          icon="↕️"
        >
          <a routerLink="/transactions/new">
            <app-button variant="primary">New Transaction</app-button>
          </a>
        </app-empty-state>
      } @else {
        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
                  <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Date</th>
                  @if (!filterWalletId()) {
                    <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Wallet</th>
                  }
                  <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Description</th>
                  <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Category</th>
                  <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Type</th>
                  <th class="px-4 py-3 text-right font-medium text-gray-600 dark:text-gray-400">Amount</th>
                  <th class="px-4 py-3 text-right font-medium text-gray-600 dark:text-gray-400">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (tx of transactions(); track tx.id) {
                  <tr class="border-b border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors">
                    <td class="px-4 py-3 text-gray-500 dark:text-gray-400 whitespace-nowrap">
                      {{ tx.transactionDate | date: 'mediumDate' }}
                    </td>
                    @if (!filterWalletId()) {
                      <td class="px-4 py-3 text-gray-700 dark:text-gray-300 whitespace-nowrap">
                        {{ walletName(tx.walletId) }}
                      </td>
                    }
                    <td class="px-4 py-3 text-gray-900 dark:text-gray-100 max-w-xs truncate">
                      {{ tx.description ?? '—' }}
                    </td>
                    <td class="px-4 py-3 text-gray-500 dark:text-gray-400">
                      {{ categoryLabel(tx) }}
                    </td>
                    <td class="px-4 py-3">
                      <app-badge [variant]="tx.type === 'INCOME' ? 'success' : 'danger'">
                        {{ tx.type === 'INCOME' ? 'Income' : 'Expense' }}
                      </app-badge>
                    </td>
                    <td class="px-4 py-3 text-right font-medium whitespace-nowrap"
                        [class]="tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400'">
                      {{ tx.type === 'INCOME' ? '+' : '-' }}{{ tx.amount | number: '1.2-2' }} {{ tx.currency }}
                    </td>
                    <td class="px-4 py-3 text-right whitespace-nowrap">
                      <div class="flex justify-end gap-2">
                        <a [routerLink]="['/transactions', tx.id, 'edit']">
                          <app-button size="sm" variant="ghost">Edit</app-button>
                        </a>
                        <app-button size="sm" variant="danger" (click)="confirmDelete(tx)">Delete</app-button>
                      </div>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>

          @if (totalPages() > 1) {
            <div class="p-4 border-t border-gray-200 dark:border-gray-700">
              <app-pagination
                [currentPage]="currentPage() + 1"
                [totalPages]="totalPages()"
                [totalElements]="totalElements()"
                [pageSize]="pageSize"
                (pageChange)="goToPage($event - 1)"
              />
            </div>
          }
        </div>
      }
    </div>

    <app-confirmation-dialog
      [isOpen]="deleteDialogOpen()"
      title="Delete Transaction"
      [message]="deleteMessage()"
      confirmLabel="Delete"
      (confirmed)="executeDelete()"
      (cancelled)="cancelDelete()"
    />
  `,
})
export class TransactionListComponent {
  private readonly txApi = inject(TransactionApiService);
  private readonly walletApi = inject(WalletApiService);
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly transactions = signal<TransactionResponse[]>([]);
  protected readonly wallets = signal<WalletResponse[]>([]);
  protected readonly categories = signal<CategoryResponse[]>([]);

  protected readonly currentPage = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly totalElements = signal(0);
  protected readonly pageSize = 20;

  protected readonly filterType = signal<TransactionType | ''>('');
  protected readonly filterWalletId = signal('');
  protected readonly filterCategoryId = signal('');
  protected readonly filterFrom = signal('');
  protected readonly filterTo = signal('');

  protected readonly deleteDialogOpen = signal(false);
  protected readonly txToDelete = signal<TransactionResponse | null>(null);

  protected readonly topLevelCategories = computed(() =>
    this.categories().filter((c) => c.parentId === null),
  );

  protected readonly hasActiveFilters = computed(
    () =>
      !!this.filterType() ||
      !!this.filterWalletId() ||
      !!this.filterCategoryId() ||
      !!this.filterFrom() ||
      !!this.filterTo(),
  );

  protected readonly deleteMessage = computed(() => {
    const tx = this.txToDelete();
    if (!tx) return '';
    return `Delete ${tx.type === TransactionType.INCOME ? 'income' : 'expense'} of ${tx.amount} ${tx.currency}? This cannot be undone.`;
  });

  protected readonly typeOptions = [
    { label: 'All', value: '' as TransactionType | '' },
    { label: 'Income', value: TransactionType.INCOME },
    { label: 'Expense', value: TransactionType.EXPENSE },
  ];

  protected readonly activeFilterBtnClass =
    'px-3 py-1.5 text-sm rounded-lg bg-indigo-600 text-white font-medium';
  protected readonly inactiveFilterBtnClass =
    'px-3 py-1.5 text-sm rounded-lg border border-gray-300 dark:border-gray-600 text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors';

  constructor() {
    this.walletApi.getAll().subscribe({ next: (ws) => this.wallets.set(ws) });
    this.categoryApi.getAll().subscribe({ next: (cs) => this.categories.set(cs) });
    this.loadTransactions(0);
  }

  protected reload(): void {
    this.loadTransactions(0);
  }

  protected loadTransactions(page: number): void {
    this.loading.set(true);
    this.currentPage.set(page);

    const obs = this.filterWalletId()
      ? this.txApi.getByWallet(this.filterWalletId(), page, this.pageSize)
      : this.txApi.getAll(page, this.pageSize, {
          type: this.filterType() || undefined,
          categoryId: this.filterCategoryId() || undefined,
          from: this.filterFrom() ? `${this.filterFrom()}T00:00:00Z` : undefined,
          to: this.filterTo() ? `${this.filterTo()}T23:59:59Z` : undefined,
        });

    obs.subscribe({
      next: (result) => {
        this.transactions.set(result.content);
        this.totalPages.set(result.totalPages);
        this.totalElements.set(result.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load transactions');
        this.loading.set(false);
      },
    });
  }

  protected goToPage(page: number): void {
    this.loadTransactions(page);
  }

  protected setTypeFilter(value: TransactionType | ''): void {
    this.filterType.set(value);
    this.loadTransactions(0);
  }

  protected setWalletFilter(value: string): void {
    this.filterWalletId.set(value);
    this.loadTransactions(0);
  }

  protected setCategoryFilter(value: string): void {
    this.filterCategoryId.set(value);
    this.loadTransactions(0);
  }

  protected clearFilters(): void {
    this.filterType.set('');
    this.filterWalletId.set('');
    this.filterCategoryId.set('');
    this.filterFrom.set('');
    this.filterTo.set('');
    this.loadTransactions(0);
  }

  protected walletName(walletId: string): string {
    return this.wallets().find((w) => w.id === walletId)?.name ?? walletId;
  }

  protected categoryLabel(tx: TransactionResponse): string {
    if (tx.subCategoryName) return `${tx.categoryName} › ${tx.subCategoryName}`;
    return tx.categoryName ?? '—';
  }

  protected confirmDelete(tx: TransactionResponse): void {
    this.txToDelete.set(tx);
    this.deleteDialogOpen.set(true);
  }

  protected cancelDelete(): void {
    this.txToDelete.set(null);
    this.deleteDialogOpen.set(false);
  }

  protected executeDelete(): void {
    const tx = this.txToDelete();
    if (!tx) return;
    this.txApi.delete(tx.id).subscribe({
      next: () => {
        this.transactions.update((list) => list.filter((t) => t.id !== tx.id));
        this.totalElements.update((n) => n - 1);
        this.toast.success('Transaction deleted');
        this.cancelDelete();
      },
      error: () => {
        this.toast.error('Failed to delete transaction');
        this.cancelDelete();
      },
    });
  }
}
