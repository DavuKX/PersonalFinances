import { Component, computed, inject, signal } from '@angular/core';
import { DecimalPipe, LowerCasePipe } from '@angular/common';
import { Router } from '@angular/router';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { WalletResponse } from '../../../core/models/wallet.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { WalletFormDialogComponent } from '../wallet-form-dialog/wallet-form-dialog.component';

@Component({
  selector: 'app-wallet-list',
  imports: [
    DecimalPipe,
    LowerCasePipe,
    SpinnerComponent,
    EmptyStateComponent,
    ButtonComponent,
    BadgeComponent,
    ConfirmationDialogComponent,
    WalletFormDialogComponent,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Wallets</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Manage your wallets and balances</p>
        </div>
        <app-button variant="primary" (click)="openCreateDialog()">+ New Wallet</app-button>
      </div>

      <label class="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 cursor-pointer select-none w-fit">
        <input
          type="checkbox"
          [checked]="showArchived()"
          (change)="showArchived.update((v) => !v)"
          class="rounded border-gray-300 dark:border-gray-600 text-indigo-600"
        />
        Show archived wallets
      </label>

      @if (loading()) {
        <div class="flex justify-center py-12">
          <app-spinner size="lg" />
        </div>
      } @else if (visibleWallets().length === 0) {
        <app-empty-state
          title="No wallets yet"
          description="Create your first wallet to start tracking your finances."
          icon="💳"
        >
          <app-button variant="primary" (click)="openCreateDialog()">Create Wallet</app-button>
        </app-empty-state>
      } @else {
        <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          @for (wallet of visibleWallets(); track wallet.id) {
            <div
              class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-5 hover:shadow-md dark:hover:border-gray-600 transition-all cursor-pointer relative"
              (click)="navigateToDetail(wallet.id)"
            >
              @if (wallet.archived) {
                <span class="absolute top-3 right-3">
                  <app-badge variant="warning">Archived</app-badge>
                </span>
              }
              <div class="mb-3">
                <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100 truncate pr-20">{{ wallet.name }}</h3>
                <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{{ wallet.currency }}</p>
              </div>
              <p class="text-2xl font-bold text-gray-900 dark:text-gray-100 mb-3">
                {{ wallet.balance | number: '1.2-2' }}
                <span class="text-base font-normal text-gray-500">{{ wallet.currency }}</span>
              </p>
              @if (wallet.spendingLimitAmount) {
                <p class="text-xs text-gray-500 dark:text-gray-400 mb-3">
                  Limit: <span class="font-medium text-gray-700 dark:text-gray-300">
                    {{ wallet.spendingLimitAmount | number: '1.2-2' }} {{ wallet.currency }} / {{ wallet.spendingLimitPeriod | lowercase }}
                  </span>
                </p>
              }
              <div class="flex gap-2" (click)="$event.stopPropagation()">
                @if (wallet.archived) {
                  <app-button size="sm" variant="secondary" (click)="restore(wallet)">Restore</app-button>
                } @else {
                  <app-button size="sm" variant="ghost" (click)="archive(wallet)">Archive</app-button>
                }
                <app-button size="sm" variant="danger" (click)="confirmDelete(wallet)">Delete</app-button>
              </div>
            </div>
          }
        </div>
      }
    </div>

    <app-wallet-form-dialog
      [isOpen]="formDialogOpen()"
      (saved)="onWalletSaved($event)"
      (closed)="formDialogOpen.set(false)"
    />

    <app-confirmation-dialog
      [isOpen]="deleteDialogOpen()"
      title="Delete Wallet"
      [message]="deleteMessage()"
      confirmLabel="Delete"
      (confirmed)="executeDelete()"
      (cancelled)="cancelDelete()"
    />
  `,
})
export class WalletListComponent {
  private readonly walletApi = inject(WalletApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly wallets = signal<WalletResponse[]>([]);
  protected readonly showArchived = signal(false);
  protected readonly formDialogOpen = signal(false);
  protected readonly deleteDialogOpen = signal(false);
  protected readonly walletToDelete = signal<WalletResponse | null>(null);

  protected readonly visibleWallets = computed(() =>
    this.showArchived() ? this.wallets() : this.wallets().filter((w) => !w.archived),
  );

  protected readonly deleteMessage = computed(() => {
    const name = this.walletToDelete()?.name ?? '';
    return `Delete wallet "${name}"? This action cannot be undone.`;
  });

  constructor() {
    this.loadWallets();
  }

  private loadWallets(): void {
    this.loading.set(true);
    this.walletApi.getAll().subscribe({
      next: (wallets) => {
        this.wallets.set(wallets);
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load wallets');
        this.loading.set(false);
      },
    });
  }

  protected openCreateDialog(): void {
    this.formDialogOpen.set(true);
  }

  protected onWalletSaved(wallet: WalletResponse): void {
    this.wallets.update((list) => {
      const idx = list.findIndex((w) => w.id === wallet.id);
      return idx >= 0 ? list.map((w) => (w.id === wallet.id ? wallet : w)) : [...list, wallet];
    });
  }

  protected navigateToDetail(id: string): void {
    this.router.navigate(['/wallets', id]);
  }

  protected archive(wallet: WalletResponse): void {
    this.walletApi.archive(wallet.id).subscribe({
      next: (updated) => {
        this.wallets.update((list) => list.map((w) => (w.id === updated.id ? updated : w)));
        this.toast.success(`"${updated.name}" archived`);
      },
      error: () => this.toast.error('Failed to archive wallet'),
    });
  }

  protected restore(wallet: WalletResponse): void {
    this.walletApi.restore(wallet.id).subscribe({
      next: (updated) => {
        this.wallets.update((list) => list.map((w) => (w.id === updated.id ? updated : w)));
        this.toast.success(`"${updated.name}" restored`);
      },
      error: () => this.toast.error('Failed to restore wallet'),
    });
  }

  protected confirmDelete(wallet: WalletResponse): void {
    this.walletToDelete.set(wallet);
    this.deleteDialogOpen.set(true);
  }

  protected cancelDelete(): void {
    this.walletToDelete.set(null);
    this.deleteDialogOpen.set(false);
  }

  protected executeDelete(): void {
    const wallet = this.walletToDelete();
    if (!wallet) return;
    this.walletApi.delete(wallet.id).subscribe({
      next: () => {
        this.wallets.update((list) => list.filter((w) => w.id !== wallet.id));
        this.toast.success('Wallet deleted');
        this.cancelDelete();
      },
      error: () => {
        this.toast.error('Failed to delete wallet');
        this.cancelDelete();
      },
    });
  }
}
