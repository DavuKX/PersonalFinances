import { Component, computed, inject, input, OnChanges, output, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { BudgetApiService } from '../../../core/services/budget-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { BudgetResponse, BudgetSummaryResponse, BudgetType, CreateBudgetRequest, UpdateBudgetRequest } from '../../../core/models/budget.models';
import { CategoryResponse } from '../../../core/models/category.models';

@Component({
  selector: 'app-budget-form-dialog',
  imports: [ReactiveFormsModule, ModalComponent, ButtonComponent, FormFieldComponent],
  template: `
    <app-modal
      [isOpen]="isOpen()"
      [title]="editBudget() ? 'Edit Budget' : 'Add Budget'"
      [hasFooter]="true"
      (closed)="closed.emit()"
    >
      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>

        @if (!editBudget()) {
          <app-form-field label="Category" fieldId="budget-category" [required]="true" [error]="categoryError()">
            <select id="budget-category" formControlName="categoryId" [class]="inputClass">
              <option value="">Select a category</option>
              @for (cat of availableCategories(); track cat.id) {
                <option [value]="cat.id">{{ cat.name }}</option>
              }
            </select>
          </app-form-field>
        }

        <app-form-field label="Budget Type" fieldId="budget-type" [required]="true">
          <div class="flex rounded-lg overflow-hidden border border-gray-300 dark:border-gray-600">
            <button type="button"
              [class]="typeTabClass('FIXED')"
              (click)="form.controls.budgetType.setValue('FIXED')">
              Fixed Amount ($)
            </button>
            <button type="button"
              [class]="typeTabClass('PERCENTAGE')"
              (click)="form.controls.budgetType.setValue('PERCENTAGE')">
              Percentage (%)
            </button>
          </div>
        </app-form-field>

        <app-form-field
          [label]="isPercentage() ? 'Percentage of Income' : 'Budget Amount'"
          fieldId="budget-amount"
          [required]="true"
          [error]="amountError()"
        >
          <div class="relative">
            @if (!isPercentage()) {
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
            }
            <input
              id="budget-amount"
              type="number"
              formControlName="amount"
              [min]="0.01"
              [max]="isPercentage() ? 100 : null"
              step="0.01"
              [placeholder]="isPercentage() ? 'e.g. 50' : '0.00'"
              [class]="isPercentage() ? inputClass : 'pl-7 ' + inputClass"
            />
            @if (isPercentage()) {
              <span class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">%</span>
            }
          </div>
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
          {{ editBudget() ? 'Save Changes' : 'Add Budget' }}
        </app-button>
      </div>
    </app-modal>
  `,
})
export class BudgetFormDialogComponent implements OnChanges {
  private readonly budgetApi = inject(BudgetApiService);
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);

  readonly isOpen = input(false);
  readonly walletId = input.required<string>();
  readonly editBudget = input<BudgetSummaryResponse | null>(null);
  readonly existingCategoryIds = input<string[]>([]);

  readonly saved = output<BudgetResponse>();
  readonly closed = output<void>();

  protected readonly loading = signal(false);
  protected readonly allCategories = signal<CategoryResponse[]>([]);

  protected readonly availableCategories = computed(() => {
    const existing = new Set(this.existingCategoryIds());
    const editing = this.editBudget()?.categoryId;
    return this.allCategories().filter(c => !existing.has(c.id) || c.id === editing);
  });

  protected readonly isPercentage = computed(
    () => this.form.controls.budgetType.value === 'PERCENTAGE',
  );

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected readonly form = new FormGroup({
    categoryId: new FormControl('', Validators.required),
    budgetType: new FormControl<string>('FIXED', Validators.required),
    amount: new FormControl<number | null>(null, [Validators.required, Validators.min(0.01)]),
  });

  constructor() {
    this.categoryApi.getAll().subscribe({ next: cats => this.allCategories.set(cats) });
  }

  ngOnChanges(): void {
    const edit = this.editBudget();
    if (edit) {
      this.form.controls.categoryId.disable();
      this.form.patchValue({
        categoryId: edit.categoryId,
        budgetType: edit.budgetType,
        amount: edit.amount,
      });
    } else {
      this.form.controls.categoryId.enable();
      this.form.reset({ budgetType: 'FIXED' });
    }
  }

  protected typeTabClass(type: string): string {
    const base = 'flex-1 py-2 text-sm font-medium transition-colors';
    return this.form.controls.budgetType.value === type
      ? `${base} bg-indigo-600 text-white`
      : `${base} bg-white dark:bg-gray-800 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700`;
  }

  protected categoryError(): string {
    const ctrl = this.form.controls.categoryId;
    if (!ctrl.dirty) return '';
    return ctrl.hasError('required') ? 'Category is required' : '';
  }

  protected amountError(): string {
    const ctrl = this.form.controls.amount;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Amount is required';
    if (ctrl.hasError('min')) return 'Amount must be greater than 0';
    return '';
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    Object.values(this.form.controls).forEach(c => c.markAsDirty());
    if (this.form.invalid) return;
    this.loading.set(true);

    const edit = this.editBudget();
    if (edit) {
      const req: UpdateBudgetRequest = {
        budgetType: this.form.value.budgetType as BudgetType,
        amount: this.form.value.amount!,
      };
      this.budgetApi.update(this.walletId(), edit.id, req).subscribe({
        next: (b) => { this.loading.set(false); this.toast.success('Budget updated'); this.saved.emit(b); this.closed.emit(); },
        error: (err) => { this.loading.set(false); this.toast.error(err?.error?.message ?? 'Failed to update budget'); },
      });
    } else {
      const req: CreateBudgetRequest = {
        categoryId: this.form.value.categoryId!,
        budgetType: this.form.value.budgetType as BudgetType,
        amount: this.form.value.amount!,
      };
      this.budgetApi.create(this.walletId(), req).subscribe({
        next: (b) => { this.loading.set(false); this.toast.success('Budget created'); this.saved.emit(b); this.closed.emit(); },
        error: (err) => { this.loading.set(false); this.toast.error(err?.error?.message ?? 'Failed to create budget'); },
      });
    }
  }
}





