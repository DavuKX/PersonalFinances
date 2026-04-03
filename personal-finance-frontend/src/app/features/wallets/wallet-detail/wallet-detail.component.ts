import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { DatePipe, DecimalPipe, LowerCasePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { LimitPeriod, SpendingSummaryResponse, WalletResponse } from '../../../core/models/wallet.models';
import { TransactionResponse } from '../../../core/models/transaction.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { WalletFormDialogComponent } from '../wallet-form-dialog/wallet-form-dialog.component';
import { SpendingLimitDialogComponent } from '../spending-limit-dialog/spending-limit-dialog.component';
import { BudgetListComponent } from '../budget-list/budget-list.component';

@Component({
  selector: 'app-wallet-detail',
  imports: [
    RouterLink,
    DecimalPipe,
    LowerCasePipe,
    DatePipe,
    SpinnerComponent,
    ButtonComponent,
    BadgeComponent,
    CardComponent,
    PaginationComponent,
    ConfirmationDialogComponent,
    EmptyStateComponent,
    WalletFormDialogComponent,
    SpendingLimitDialogComponent,
    BudgetListComponent,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex items-center gap-3">
        <a routerLink="/wallets" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors" aria-label="Back to wallets">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </a>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          {{ wallet()?.name ?? 'Wallet' }}
        </h2>
        @if (wallet()?.archived) {
          <app-badge variant="warning">Archived</app-badge>
        }
      </div>

      @if (loading()) {
        <div class="flex justify-center py-12">
          <app-spinner size="lg" />
        </div>
      } @else if (wallet()) {
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">

          <app-card title="Balance">
            <p class="text-3xl font-bold text-gray-900 dark:text-gray-100">
              {{ wallet()!.balance | number: '1.2-2' }}
              <span class="text-lg font-normal text-gray-500 dark:text-gray-400">{{ wallet()!.currency }}</span>
            </p>
            <p class="text-xs text-gray-400 dark:text-gray-500 mt-2">Created {{ wallet()!.createdAt | date: 'mediumDate' }}</p>
            <div class="flex gap-2 mt-4">
              <app-button size="sm" variant="secondary" (click)="editDialogOpen.set(true)">Edit Name</app-button>
              @if (wallet()!.archived) {
                <app-button size="sm" variant="secondary" (click)="restore()">Restore</app-button>
              } @else {
                <app-button size="sm" variant="ghost" (click)="archive()">Archive</app-button>
              }
              <app-button size="sm" variant="danger" (click)="deleteDialogOpen.set(true)">Delete</app-button>
            </div>
          </app-card>

          <app-card title="Spending Limit">
            @if (wallet()!.spendingLimitAmount) {
              <div class="space-y-3">
                <div class="flex items-end justify-between">
                  <div>
                    <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
                      {{ wallet()!.spendingLimitAmount | number: '1.2-2' }}
                      <span class="text-base font-normal text-gray-500">{{ wallet()!.currency }}</span>
                    </p>
                    <p class="text-sm text-gray-500 dark:text-gray-400">
                      per {{ wallet()!.spendingLimitPeriod | lowercase }}
                    </p>
                  </div>
                  @if (spendingSummary()) {
                    <div class="text-right">
                      <p class="text-sm font-medium" [class]="limitExceeded() ? 'text-rose-600 dark:text-rose-400' : 'text-gray-700 dark:text-gray-300'">
                        {{ spendingSummary()!.spentAmount | number: '1.2-2' }} spent
                      </p>
                      <p class="text-xs text-gray-500 dark:text-gray-400">
                        {{ remaining() | number: '1.2-2' }} remaining
                      </p>
                    </div>
                  }
                </div>

                @if (spendingSummary()) {
                  <!-- Progress bar -->
                  <div class="space-y-1">
                    <div class="h-2 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden">
                      <div class="h-full rounded-full transition-all duration-500"
                           [class]="limitExceeded() ? 'bg-rose-500' : spentPercent() >= 80 ? 'bg-amber-500' : 'bg-indigo-500'"
                           [style.width.%]="Math.min(spentPercent(), 100)">
                      </div>
                    </div>
                    <p class="text-xs text-gray-400 dark:text-gray-500 text-right">
                      {{ spentPercent() | number: '1.0-0' }}% used this {{ wallet()!.spendingLimitPeriod | lowercase }}
                    </p>
                  </div>
                }

                <div class="flex gap-2 mt-1">
                  <app-button size="sm" variant="secondary" (click)="limitDialogOpen.set(true)">Change</app-button>
                  <app-button size="sm" variant="ghost" (click)="removeSpendingLimit()">Remove</app-button>
                </div>
              </div>
            } @else {
              <p class="text-sm text-gray-500 dark:text-gray-400 mb-4">No spending limit set.</p>
              <app-button size="sm" variant="primary" (click)="limitDialogOpen.set(true)">Set Limit</app-button>
            }
          </app-card>
        </div>

        @if (!wallet()!.archived) {
          <app-budget-list [walletId]="wallet()!.id" />
        }

        <app-card title="Transactions" [noPadding]="true" class="my-6">
          @if (txLoading()) {
            <div class="flex justify-center py-10">
              <app-spinner />
            </div>
          } @else if (transactions().length === 0) {
            <div class="p-6">
              <app-empty-state
                title="No transactions yet"
                description="Transactions for this wallet will appear here."
                icon="📋"
              />
            </div>
          } @else {
            <div class="overflow-x-auto">
              <table class="w-full text-sm">
                <thead>
                  <tr class="border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800/50">
                    <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Date</th>
                    <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Description</th>
                    <th class="px-4 py-3 text-left font-medium text-gray-600 dark:text-gray-400">Category</th>
                    <th class="px-4 py-3 text-right font-medium text-gray-600 dark:text-gray-400">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  @for (tx of transactions(); track tx.id) {
                    <tr class="border-b border-gray-100 dark:border-gray-800 hover:bg-gray-50 dark:hover:bg-gray-800/30 transition-colors">
                      <td class="px-4 py-3 text-gray-500 dark:text-gray-400 whitespace-nowrap">
                        {{ tx.transactionDate | date: 'mediumDate' }}
                      </td>
                      <td class="px-4 py-3 text-gray-900 dark:text-gray-100">
                        {{ tx.description ?? '—' }}
                      </td>
                      <td class="px-4 py-3 text-gray-500 dark:text-gray-400">
                        {{ categoryLabel(tx) }}
                      </td>
                      <td class="px-4 py-3 text-right font-medium whitespace-nowrap"
                          [class]="tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : tx.type === 'SAVINGS' ? 'text-amber-600 dark:text-amber-400' : 'text-rose-600 dark:text-rose-400'">
                        {{ tx.type === 'INCOME' ? '+' : '-' }}{{ tx.amount | number: '1.2-2' }} {{ tx.currency }}
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            @if (txTotalPages() > 1) {
              <div class="p-4 border-t border-gray-200 dark:border-gray-700">
                <app-pagination
                  [currentPage]="txPage() + 1"
                  [totalPages]="txTotalPages()"
                  [totalElements]="txTotalElements()"
                  [pageSize]="txPageSize"
                  (pageChange)="loadTransactions($event - 1)"
                />
              </div>
            }
          }
        </app-card>
      } @else if (!loading()) {
        <app-empty-state title="Wallet not found" description="This wallet does not exist or you don't have access." icon="🔍" />
      }
    </div>

    @if (wallet()) {
      <app-wallet-form-dialog
        [isOpen]="editDialogOpen()"
        [wallet]="wallet()"
        (saved)="wallet.set($event)"
        (closed)="editDialogOpen.set(false)"
      />
      <app-spending-limit-dialog
        [isOpen]="limitDialogOpen()"
        [walletId]="wallet()!.id"
        (saved)="onSpendingLimitSaved($event)"
        (closed)="limitDialogOpen.set(false)"
      />
    }

    <app-confirmation-dialog
      [isOpen]="deleteDialogOpen()"
      title="Delete Wallet"
      [message]="deleteMessage()"
      confirmLabel="Delete"
      (confirmed)="executeDelete()"
      (cancelled)="deleteDialogOpen.set(false)"
    />
  `,
})
export class WalletDetailComponent {
  readonly id = input.required<string>();

  private readonly walletApi = inject(WalletApiService);
  private readonly transactionApi = inject(TransactionApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly wallet = signal<WalletResponse | null>(null);

  protected readonly txLoading = signal(false);
  protected readonly transactions = signal<TransactionResponse[]>([]);
  protected readonly txPage = signal(0);
  protected readonly txTotalPages = signal(0);
  protected readonly txTotalElements = signal(0);
  protected readonly txPageSize = 20;

  protected readonly editDialogOpen = signal(false);
  protected readonly limitDialogOpen = signal(false);
  protected readonly deleteDialogOpen = signal(false);

  protected readonly deleteMessage = computed(
    () => `Delete wallet "${this.wallet()?.name ?? ''}"? This action cannot be undone.`,
  );

  protected readonly spendingSummary = signal<SpendingSummaryResponse | null>(null);

  protected readonly spentPercent = computed(() => {
    const s = this.spendingSummary();
    const limit = this.wallet()?.spendingLimitAmount;
    if (!s || !limit || limit === 0) return 0;
    return (s.spentAmount / limit) * 100;
  });

  protected readonly remaining = computed(() => {
    const s = this.spendingSummary();
    const limit = this.wallet()?.spendingLimitAmount;
    if (!s || !limit) return 0;
    return limit - s.spentAmount;
  });

  protected readonly limitExceeded = computed(() => this.spentPercent() > 100);

  protected readonly Math = Math;

  constructor() {
    effect(() => {
      const id = this.id();
      this.loadWallet(id);
      this.loadTransactions(0, id);
    });
  }

  private loadWallet(id: string): void {
    this.loading.set(true);
    this.walletApi.getById(id).subscribe({
      next: (w) => {
        this.wallet.set(w);
        this.loading.set(false);
        if (w.spendingLimitAmount && w.spendingLimitPeriod) {
          this.loadSpendingSummary(id, w.spendingLimitPeriod);
        }
      },
      error: () => {
        this.wallet.set(null);
        this.loading.set(false);
      },
    });
  }

  private loadSpendingSummary(walletId: string, period: LimitPeriod): void {
    const { from, to } = this.currentPeriodRange(period);
    this.transactionApi.getSpendingSummary(walletId, from, to).subscribe({
      next: (s) => this.spendingSummary.set(s),
      error: () => this.spendingSummary.set(null),
    });
  }

  private currentPeriodRange(period: LimitPeriod): { from: string; to: string } {
    const now = new Date();
    let from: Date;
    let to: Date;
    if (period === LimitPeriod.DAILY) {
      from = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      to = new Date(from.getTime() + 86_400_000);
    } else if (period === LimitPeriod.WEEKLY) {
      const day = now.getDay(); // 0=Sun
      const diff = day === 0 ? -6 : 1 - day; // Monday as week start
      from = new Date(now.getFullYear(), now.getMonth(), now.getDate() + diff);
      to = new Date(from.getTime() + 7 * 86_400_000);
    } else {
      // MONTHLY
      from = new Date(now.getFullYear(), now.getMonth(), 1);
      to = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    }
    return { from: from.toISOString(), to: to.toISOString() };
  }

  protected loadTransactions(page: number, id = this.id()): void {
    this.txLoading.set(true);
    this.transactionApi.getByWallet(id, page, this.txPageSize).subscribe({
      next: (result) => {
        this.transactions.set(result.content);
        this.txPage.set(result.page);
        this.txTotalPages.set(result.totalPages);
        this.txTotalElements.set(result.totalElements);
        this.txLoading.set(false);
      },
      error: () => {
        this.txLoading.set(false);
      },
    });
  }

  protected categoryLabel(tx: TransactionResponse): string {
    if (tx.subCategoryName) return `${tx.categoryName} › ${tx.subCategoryName}`;
    return tx.categoryName ?? '—';
  }

  protected archive(): void {
    this.walletApi.archive(this.id()).subscribe({
      next: (w) => {
        this.wallet.set(w);
        this.toast.success('Wallet archived');
      },
      error: () => this.toast.error('Failed to archive wallet'),
    });
  }

  protected restore(): void {
    this.walletApi.restore(this.id()).subscribe({
      next: (w) => {
        this.wallet.set(w);
        this.toast.success('Wallet restored');
      },
      error: () => this.toast.error('Failed to restore wallet'),
    });
  }

  protected removeSpendingLimit(): void {
    this.walletApi.removeSpendingLimit(this.id()).subscribe({
      next: (w) => {
        this.wallet.set(w);
        this.spendingSummary.set(null);
        this.toast.success('Spending limit removed');
      },
      error: () => this.toast.error('Failed to remove spending limit'),
    });
  }

  protected onSpendingLimitSaved(w: WalletResponse): void {
    this.wallet.set(w);
    if (w.spendingLimitAmount && w.spendingLimitPeriod) {
      this.loadSpendingSummary(w.id, w.spendingLimitPeriod);
    }
  }

  protected executeDelete(): void {
    this.walletApi.delete(this.id()).subscribe({
      next: () => {
        this.toast.success('Wallet deleted');
        this.router.navigate(['/wallets']);
      },
      error: () => this.toast.error('Failed to delete wallet'),
    });
  }
}
