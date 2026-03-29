import { Component, effect, inject, input, output, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';

@Component({
  selector: 'app-category-form-dialog',
  imports: [ReactiveFormsModule, ModalComponent, ButtonComponent, FormFieldComponent],
  template: `
    <app-modal
      [isOpen]="isOpen()"
      title="New Category"
      [hasFooter]="true"
      (closed)="closed.emit()"
    >
      <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
        <app-form-field label="Name" fieldId="cat-name" [error]="nameError()" [required]="true">
          <input
            id="cat-name"
            type="text"
            formControlName="name"
            placeholder="e.g. Groceries"
            [class]="inputClass"
          />
        </app-form-field>

        <app-form-field label="Type" fieldId="cat-type" [required]="true">
          <select
            id="cat-type"
            formControlName="transactionType"
            [class]="inputClass"
            [attr.disabled]="fixedType() ? true : null"
          >
            <option value="INCOME">Income</option>
            <option value="EXPENSE">Expense</option>
          </select>
        </app-form-field>

        @if (availableParents().length > 0) {
          <app-form-field label="Parent Category" fieldId="cat-parent" hint="Leave empty to create a top-level category">
            <select id="cat-parent" formControlName="parentId" [class]="inputClass">
              <option value="">— None (top-level) —</option>
              @for (parent of availableParents(); track parent.id) {
                <option [value]="parent.id">{{ parent.name }}</option>
              }
            </select>
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
          Create Category
        </app-button>
      </div>
    </app-modal>
  `,
})
export class CategoryFormDialogComponent {
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);

  readonly isOpen = input(false);
  readonly fixedType = input<TransactionType | null>(null);
  readonly availableParents = input<CategoryResponse[]>([]);

  readonly saved = output<CategoryResponse>();
  readonly closed = output<void>();

  protected readonly loading = signal(false);

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected readonly form = new FormGroup({
    name: new FormControl('', [Validators.required, Validators.maxLength(100)]),
    transactionType: new FormControl<TransactionType>(TransactionType.EXPENSE, Validators.required),
    parentId: new FormControl<string>(''),
  });

  constructor() {
    effect(() => {
      const fixed = this.fixedType();
      if (fixed) {
        this.form.controls.transactionType.setValue(fixed);
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

  protected submit(): void {
    this.form.controls.name.markAsDirty();
    if (this.form.invalid) return;
    this.loading.set(true);
    this.categoryApi
      .create({
        name: this.form.value.name!,
        transactionType: this.form.value.transactionType!,
        parentId: this.form.value.parentId || null,
      })
      .subscribe({
        next: (category) => {
          this.loading.set(false);
          this.toast.success('Category created');
          this.saved.emit(category);
          this.closed.emit();
          this.form.reset({ name: '', transactionType: TransactionType.EXPENSE, parentId: '' });
        },
        error: () => {
          this.loading.set(false);
          this.toast.error('Failed to create category');
        },
      });
  }
}

