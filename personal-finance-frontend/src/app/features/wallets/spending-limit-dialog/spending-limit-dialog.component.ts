import { Component, inject, input, output, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { WalletApiService } from '../../../core/services/wallet-api.service';
import { LimitPeriod, WalletResponse } from '../../../core/models/wallet.models';

@Component({
  selector: 'app-spending-limit-dialog',
  imports: [ReactiveFormsModule, ModalComponent, ButtonComponent, FormFieldComponent],
  template: `
    <app-modal
      [isOpen]="isOpen()"
      title="Set Spending Limit"
      [hasFooter]="true"
      (closed)="closed.emit()"
    >
      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
        <app-form-field
          label="Limit Amount"
          fieldId="limit-amount"
          [error]="amountError()"
          [required]="true"
        >
          <input
            id="limit-amount"
            type="number"
            formControlName="amount"
            min="0.01"
            step="0.01"
            placeholder="0.00"
            [class]="inputClass"
          />
        </app-form-field>

        <app-form-field label="Period" fieldId="limit-period" [required]="true" class="m-2">
          <select id="limit-period" formControlName="period" [class]="inputClass">
            <option value="DAILY">Daily</option>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
          </select>
        </app-form-field>
      </form>

      <div modal-footer class="flex justify-end gap-3">
        <app-button variant="secondary" (click)="closed.emit()">Cancel</app-button>
        <app-button
          variant="primary"
          [loading]="loading()"
          [disabled]="form.invalid"
          (click)="submit()"
        >
          Set Limit
        </app-button>
      </div>
    </app-modal>
  `,
})
export class SpendingLimitDialogComponent {
  private readonly walletApi = inject(WalletApiService);
  private readonly toast = inject(ToastService);

  readonly isOpen = input(false);
  readonly walletId = input.required<string>();

  readonly saved = output<WalletResponse>();
  readonly closed = output<void>();

  protected readonly loading = signal(false);

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected readonly form = new FormGroup({
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
    period: new FormControl<LimitPeriod>(LimitPeriod.MONTHLY, Validators.required),
  });

  protected amountError(): string {
    const ctrl = this.form.controls.amount;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Amount is required';
    if (ctrl.hasError('min')) return 'Amount must be greater than 0';
    return '';
  }

  protected submit(): void {
    this.form.controls.amount.markAsDirty();
    if (this.form.invalid) return;
    this.loading.set(true);
    this.walletApi
      .setSpendingLimit(this.walletId(), {
        amount: this.form.value.amount!,
        period: this.form.value.period!,
      })
      .subscribe({
        next: (wallet) => {
          this.loading.set(false);
          this.toast.success('Spending limit set');
          this.saved.emit(wallet);
          this.closed.emit();
        },
        error: () => {
          this.loading.set(false);
          this.toast.error('Failed to set spending limit');
        },
      });
  }
}

