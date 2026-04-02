import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { TransactionApiService } from '../../../core/services/transaction-api.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { TransactionType } from '../../../core/models/transaction.models';
import { WalletResponse } from '../../../core/models/wallet.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';

function todayString(): string {
  return new Date().toISOString().slice(0, 10);
}

@Component({
  selector: 'app-transaction-form',
  imports: [RouterLink, ReactiveFormsModule, SpinnerComponent, ButtonComponent, FormFieldComponent],
  template: `
    <div class="space-y-6 max-w-2xl">
      <div class="flex items-center gap-3">
        <a routerLink="/transactions" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors" aria-label="Back">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
        </a>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          {{ isEditMode() ? 'Edit Transaction' : 'New Transaction' }}
        </h2>
      </div>

      @if (loading()) {
        <div class="flex justify-center py-12">
          <app-spinner size="lg" />
        </div>
      } @else {
        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-6">
          <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-5" novalidate>

            <app-form-field label="Wallet" fieldId="tx-wallet" [error]="walletError()" [required]="true">
              <select
                id="tx-wallet"
                formControlName="walletId"
                [attr.disabled]="isEditMode() ? true : null"
                [class]="inputClass"
              >
                <option value="">— Select a wallet —</option>
                @for (w of wallets(); track w.id) {
                  <option [value]="w.id">{{ w.name }} ({{ w.currency }})</option>
                }
              </select>
            </app-form-field>

            <app-form-field label="Type" fieldId="tx-type" [required]="true">
              <div class="flex gap-2">
                <button
                  type="button"
                  (click)="changeType(TransactionType.INCOME)"
                  [class]="form.value.type === TransactionType.INCOME ? activeTypeClass : inactiveTypeClass"
                >
                  ↑ Income
                </button>
                <button
                  type="button"
                  (click)="changeType(TransactionType.EXPENSE)"
                  [class]="form.value.type === TransactionType.EXPENSE ? activeTypeClass : inactiveTypeClass"
                >
                  ↓ Expense
                </button>
                <button
                  type="button"
                  (click)="changeType(TransactionType.SAVINGS)"
                  [class]="form.value.type === TransactionType.SAVINGS ? activeTypeClass : inactiveTypeClass"
                >
                  🏦 Savings
                </button>
              </div>
            </app-form-field>

            <div class="grid grid-cols-2 gap-4">
              <app-form-field label="Amount" fieldId="tx-amount" [error]="amountError()" [required]="true">
                <input
                  id="tx-amount"
                  type="number"
                  formControlName="amount"
                  min="0.01"
                  step="0.01"
                  placeholder="0.00"
                  [class]="inputClass"
                />
              </app-form-field>
              <app-form-field label="Currency" fieldId="tx-currency">
                <input
                  id="tx-currency"
                  type="text"
                  [value]="selectedWallet()?.currency ?? ''"
                  readonly
                  class="w-full px-3 py-2 text-sm bg-gray-50 dark:bg-gray-800/50 text-gray-500 dark:text-gray-400 border border-gray-300 dark:border-gray-600 rounded-lg cursor-not-allowed"
                />
              </app-form-field>
            </div>

            <app-form-field label="Category" fieldId="tx-category">
              <select
                id="tx-category"
                formControlName="categoryId"
                (change)="form.controls.subCategoryId.setValue('')"
                [class]="inputClass"
              >
                <option value="">— No category —</option>
                @for (c of filteredCategories(); track c.id) {
                  <option [value]="c.id">{{ c.name }}</option>
                }
              </select>
            </app-form-field>

            @if (filteredSubcategories().length > 0) {
              <app-form-field label="Subcategory" fieldId="tx-subcategory">
                <select id="tx-subcategory" formControlName="subCategoryId" [class]="inputClass">
                  <option value="">— No subcategory —</option>
                  @for (s of filteredSubcategories(); track s.id) {
                    <option [value]="s.id">{{ s.name }}</option>
                  }
                </select>
              </app-form-field>
            }

            <app-form-field label="Description" fieldId="tx-description">
              <input
                id="tx-description"
                type="text"
                formControlName="description"
                placeholder="Optional note"
                [class]="inputClass"
              />
            </app-form-field>

            <app-form-field label="Date" fieldId="tx-date" [error]="dateError()" [required]="true">
              <input
                id="tx-date"
                type="date"
                formControlName="transactionDate"
                [class]="inputClass"
              />
            </app-form-field>

            <div class="flex justify-end gap-3 pt-2">
              <a routerLink="/transactions">
                <app-button type="button" variant="secondary">Cancel</app-button>
              </a>
              <app-button
                type="submit"
                variant="primary"
                [loading]="saving()"
                [disabled]="form.invalid"
              >
                {{ isEditMode() ? 'Save Changes' : 'Create Transaction' }}
              </app-button>
            </div>
          </form>
        </div>
      }
    </div>
  `,
})
export class TransactionFormComponent {
  readonly id = input<string | undefined>(undefined);

  private readonly txApi = inject(TransactionApiService);
  private readonly walletApi = inject(WalletApiService);
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly TransactionType = TransactionType;

  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly wallets = signal<WalletResponse[]>([]);
  protected readonly categories = signal<CategoryResponse[]>([]);

  protected readonly form = new FormGroup({
    walletId: new FormControl('', Validators.required),
    type: new FormControl<TransactionType>(TransactionType.EXPENSE, Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    categoryId: new FormControl(''),
    subCategoryId: new FormControl(''),
    description: new FormControl(''),
    transactionDate: new FormControl(todayString(), Validators.required),
  });

  private readonly typeSignal = toSignal(this.form.controls.type.valueChanges, {
    initialValue: this.form.controls.type.value!,
  });
  private readonly categoryIdSignal = toSignal(this.form.controls.categoryId.valueChanges, {
    initialValue: '',
  });
  private readonly walletIdSignal = toSignal(this.form.controls.walletId.valueChanges, {
    initialValue: '',
  });

  protected readonly isEditMode = computed(() => !!this.id());

  protected readonly selectedWallet = computed(
    () => this.wallets().find((w) => w.id === this.walletIdSignal()) ?? null,
  );

  protected readonly filteredCategories = computed(() =>
    this.categories().filter(
      (c) => c.transactionType === this.typeSignal() && c.parentId === null,
    ),
  );

  protected readonly filteredSubcategories = computed(() => {
    const catId = this.categoryIdSignal();
    if (!catId) return [];
    return this.categories().filter((c) => c.parentId === catId);
  });

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected readonly activeTypeClass =
    'px-4 py-2 text-sm font-medium rounded-lg bg-indigo-600 text-white';
  protected readonly inactiveTypeClass =
    'px-4 py-2 text-sm font-medium rounded-lg border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 transition-colors';

  constructor() {
    this.walletApi
      .getAll()
      .subscribe({ next: (ws) => this.wallets.set(ws.filter((w) => !w.archived)) });
    this.categoryApi.getAll().subscribe({ next: (cs) => this.categories.set(cs) });

    effect(() => {
      const id = this.id();
      if (id) this.loadTransaction(id);
    });
  }

  private loadTransaction(id: string): void {
    this.loading.set(true);
    this.txApi.getById(id).subscribe({
      next: (tx) => {
        this.form.patchValue({
          walletId: tx.walletId,
          type: tx.type,
          amount: tx.amount,
          categoryId: tx.categoryId ?? '',
          subCategoryId: tx.subCategoryId ?? '',
          description: tx.description ?? '',
          transactionDate: tx.transactionDate.slice(0, 10),
        });
        this.form.controls.walletId.disable();
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load transaction');
        this.router.navigate(['/transactions']);
      },
    });
  }

  protected changeType(type: TransactionType): void {
    this.form.patchValue({ type, categoryId: '', subCategoryId: '' });
  }

  protected walletError(): string {
    const ctrl = this.form.controls.walletId;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Wallet is required';
    return '';
  }

  protected amountError(): string {
    const ctrl = this.form.controls.amount;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Amount is required';
    if (ctrl.hasError('min')) return 'Amount must be greater than 0';
    return '';
  }

  protected dateError(): string {
    const ctrl = this.form.controls.transactionDate;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Date is required';
    return '';
  }

  protected submit(): void {
    this.form.controls.walletId.markAsDirty();
    this.form.controls.amount.markAsDirty();
    this.form.controls.transactionDate.markAsDirty();
    if (this.form.invalid) return;

    this.saving.set(true);
    const v = this.form.getRawValue();
    const dateIso = `${v.transactionDate}T00:00:00Z`;

    const obs = this.isEditMode()
      ? this.txApi.update(this.id()!, {
          type: v.type!,
          amount: v.amount!,
          categoryId: v.categoryId || null,
          subCategoryId: v.subCategoryId || null,
          description: v.description || null,
          transactionDate: dateIso,
        })
      : this.txApi.create({
          walletId: v.walletId!,
          type: v.type!,
          amount: v.amount!,
          currency: this.selectedWallet()?.currency ?? '',
          categoryId: v.categoryId || null,
          subCategoryId: v.subCategoryId || null,
          description: v.description || null,
          transactionDate: dateIso,
        });

    obs.subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success(this.isEditMode() ? 'Transaction updated' : 'Transaction created');
        this.router.navigate(['/transactions']);
      },
      error: () => {
        this.saving.set(false);
        this.toast.error(this.isEditMode() ? 'Failed to update transaction' : 'Failed to create transaction');
      },
    });
  }
}
