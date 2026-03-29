import { Component, effect, inject, input, output, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { WalletResponse } from '../../../core/models/wallet.models';

@Component({
  selector: 'app-wallet-form-dialog',
  imports: [ReactiveFormsModule, ModalComponent, ButtonComponent, FormFieldComponent],
  template: `
    <app-modal
      [isOpen]="isOpen()"
      [title]="wallet() ? 'Edit Wallet' : 'New Wallet'"
      [hasFooter]="true"
      (closed)="closed.emit()"
    >
      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
        <app-form-field label="Name" fieldId="wallet-name" [error]="nameError()" [required]="true">
          <input
            id="wallet-name"
            type="text"
            formControlName="name"
            placeholder="e.g. Main Account"
            [class]="inputClass"
          />
        </app-form-field>

        @if (!wallet()) {
          <app-form-field
            label="Currency"
            fieldId="wallet-currency"
            [error]="currencyError()"
            [required]="true"
            hint="3-letter ISO code, e.g. USD"
          >
            <input
              id="wallet-currency"
              type="text"
              formControlName="currency"
              placeholder="USD"
              maxlength="3"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field
            label="Initial Balance"
            fieldId="wallet-balance"
            [error]="balanceError()"
            [required]="true"
          >
            <input
              id="wallet-balance"
              type="number"
              formControlName="balance"
              min="0"
              step="0.01"
              placeholder="0.00"
              [class]="inputClass"
            />
          </app-form-field>
        }
      </form>

      <div modal-footer class="flex justify-end gap-3">
        <app-button variant="secondary" (click)="closed.emit()">Cancel</app-button>
        <app-button
          variant="primary"
          [loading]="loading()"
          [disabled]="form.invalid"
          (click)="submit()"
        >
          {{ wallet() ? 'Save Changes' : 'Create Wallet' }}
        </app-button>
      </div>
    </app-modal>
  `,
})
export class WalletFormDialogComponent {
  private readonly walletApi = inject(WalletApiService);
  private readonly toast = inject(ToastService);

  readonly isOpen = input(false);
  readonly wallet = input<WalletResponse | null>(null);

  readonly saved = output<WalletResponse>();
  readonly closed = output<void>();

  protected readonly loading = signal(false);

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected readonly form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(100)]),
    currency: new FormControl('', [Validators.required, Validators.pattern(/^[A-Za-z]{3}$/)]),
    balance: new FormControl<number>(0, [Validators.required, Validators.min(0)]),
  });

  constructor() {
    effect(() => {
      const w = this.wallet();
      if (w) {
        this.form.patchValue({ name: w.name });
        this.form.controls.currency.disable();
        this.form.controls.balance.disable();
      } else {
        this.form.reset({ name: '', currency: '', balance: 0 });
        this.form.controls.currency.enable();
        this.form.controls.balance.enable();
      }
    });
  }

  protected nameError(): string {
    const ctrl = this.form.controls.name;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Name is required';
    if (ctrl.hasError('maxlength')) return 'Name must not exceed 100 characters';
    return '';
  }

  protected currencyError(): string {
    const ctrl = this.form.controls.currency;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Currency is required';
    if (ctrl.hasError('pattern')) return 'Enter a valid 3-letter ISO currency code (e.g. USD)';
    return '';
  }

  protected balanceError(): string {
    const ctrl = this.form.controls.balance;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Balance is required';
    if (ctrl.hasError('min')) return 'Balance must be 0 or greater';
    return '';
  }

  protected submit(): void {
    this.form.controls.name.markAsDirty();
    if (!this.wallet()) {
      this.form.controls.currency.markAsDirty();
      this.form.controls.balance.markAsDirty();
    }
    if (this.form.invalid) return;

    const w = this.wallet();
    this.loading.set(true);

    const obs = w
      ? this.walletApi.update(w.id, { name: this.form.value.name! })
      : this.walletApi.create({
          name: this.form.value.name!,
          currency: this.form.value.currency!.toUpperCase(),
          balance: this.form.value.balance ?? 0,
        });

    obs.subscribe({
      next: (wallet) => {
        this.loading.set(false);
        this.toast.success(w ? 'Wallet updated' : 'Wallet created');
        this.saved.emit(wallet);
        this.closed.emit();
      },
      error: () => {
        this.loading.set(false);
        this.toast.error(w ? 'Failed to update wallet' : 'Failed to create wallet');
      },
    });
  }
}

