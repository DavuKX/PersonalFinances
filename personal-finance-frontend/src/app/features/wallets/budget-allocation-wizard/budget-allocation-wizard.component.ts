import { Component, inject, input, output, signal, computed, OnDestroy } from '@angular/core';
import { ReactiveFormsModule, FormArray, FormControl, FormGroup, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { DecimalPipe } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { BudgetApiService } from '../../../core/services/budget-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { BudgetResponse, BudgetType, BulkBudgetRequest } from '../../../core/models/budget.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';

const PRESET_TEMPLATES: { name: string; allocations: { pct: number; label: string }[] }[] = [
  {
    name: '50/30/20',
    allocations: [
      { pct: 50, label: 'Needs' },
      { pct: 30, label: 'Personal' },
      { pct: 20, label: 'Savings' },
    ],
  },
  {
    name: '70/20/10',
    allocations: [
      { pct: 70, label: 'Living' },
      { pct: 20, label: 'Savings' },
      { pct: 10, label: 'Debt' },
    ],
  },
];

@Component({
  selector: 'app-budget-allocation-wizard',
  imports: [ReactiveFormsModule, DecimalPipe, ModalComponent, ButtonComponent, FormFieldComponent],
  template: `
    <app-modal
      [isOpen]="isOpen()"
      title="Budget Allocation Wizard"
      [hasFooter]="true"
      (closed)="closed.emit()"
    >
      <div class="space-y-6">
        <!-- Step indicator -->
        <div class="flex items-center gap-2 text-sm">
          @for (s of steps; track s.n) {
            <div class="flex items-center gap-2">
              <div [class]="stepCircle(s.n)">{{ s.n }}</div>
              <span [class]="step() === s.n ? 'font-medium text-gray-900 dark:text-gray-100' : 'text-gray-400'">{{ s.label }}</span>
            </div>
            @if (s.n < steps.length) {
              <div class="flex-1 h-px bg-gray-200 dark:bg-gray-700 mx-1"></div>
            }
          }
        </div>

        <!-- Step 1: Income -->
        @if (step() === 1) {
          <div class="space-y-4">
            <p class="text-sm text-gray-600 dark:text-gray-400">
              Enter your monthly income to calculate budget allocations as percentages.
            </p>
            <app-form-field label="Monthly Income" fieldId="monthly-income" [required]="true">
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
                <input
                  id="monthly-income"
                  type="number"
                  [formControl]="incomeCtrl"
                  min="1"
                  step="0.01"
                  placeholder="10,000.00"
                  [class]="'pl-7 ' + inputClass"
                />
              </div>
            </app-form-field>
            <div class="space-y-2">
              <p class="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">Quick presets</p>
              <div class="flex gap-2 flex-wrap">
                @for (t of templates; track t.name) {
                  <button type="button"
                    class="px-3 py-1.5 text-xs font-medium rounded-lg border border-indigo-200 dark:border-indigo-700 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-50 dark:hover:bg-indigo-900/20 transition-colors"
                    (click)="selectedTemplate.set(t)">
                    {{ t.name }} Rule
                  </button>
                }
              </div>
              @if (selectedTemplate()) {
                <p class="text-xs text-emerald-600 dark:text-emerald-400">
                  ✓ Template "{{ selectedTemplate()!.name }}" selected — allocations will be pre-filled in step 2.
                </p>
              }
            </div>
          </div>
        }

        <!-- Step 2: Allocations -->
        @if (step() === 2) {
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <p class="text-sm text-gray-600 dark:text-gray-400">
                Allocate percentages across your expense categories. Total must not exceed 100%.
              </p>
              <span [class]="totalBadgeClass()">{{ totalPct() | number: '1.0-1' }}%</span>
            </div>

            <!-- Progress bar -->
            <div class="h-2 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden">
              <div class="h-full rounded-full transition-all"
                   [class]="totalPct() > 100 ? 'bg-rose-500' : totalPct() >= 90 ? 'bg-amber-500' : 'bg-indigo-500'"
                   [style.width.%]="Math.min(totalPct(), 100)">
              </div>
            </div>

            <div class="space-y-2 max-h-64 overflow-y-auto pr-1">
              @for (row of allocationRows().controls; track $index; let i = $index) {
                <div [formGroup]="row" class="flex items-center gap-3 p-3 rounded-lg bg-gray-50 dark:bg-gray-800/50">
                  <div class="flex-1 min-w-0">
                    <select formControlName="categoryId" [class]="'text-xs ' + inputClass">
                      <option value="">— Select category —</option>
                      @if (expenseCategories().length) {
                        <optgroup label="Expenses">
                          @for (cat of expenseCategories(); track cat.id) {
                            <option [value]="cat.id">{{ cat.name }}</option>
                          }
                        </optgroup>
                      }
                      @if (savingsCategories().length) {
                        <optgroup label="Savings">
                          @for (cat of savingsCategories(); track cat.id) {
                            <option [value]="cat.id">{{ cat.name }}</option>
                          }
                        </optgroup>
                      }
                    </select>
                  </div>
                  <div class="relative w-28 shrink-0">
                    <input type="number" formControlName="pct" min="0.1" max="100" step="0.1"
                           placeholder="0" [class]="inputClass" />
                    <span class="absolute right-2 top-1/2 -translate-y-1/2 text-gray-400 text-xs">%</span>
                  </div>
                  @if (incomeCtrl.value) {
                    <span class="text-xs text-gray-400 shrink-0 w-20 text-right">
                      \${{ (row.value.pct / 100) * incomeCtrl.value! | number: '1.0-0' }}
                    </span>
                  }
                  <button type="button" (click)="removeRow(i)"
                    class="text-gray-400 hover:text-rose-500 dark:hover:text-rose-400 transition-colors shrink-0">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              }
              <button type="button" (click)="addRow()"
                class="w-full py-2 text-sm text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-200 border-2 border-dashed border-indigo-200 dark:border-indigo-800 rounded-lg transition-colors">
                + Add category
              </button>
            </div>
          </div>
        }

        <!-- Step 3: Review -->
        @if (step() === 3) {
          <div class="space-y-3">
            <p class="text-sm text-gray-600 dark:text-gray-400">
              Review your budget allocations for a monthly income of
              <strong class="text-gray-900 dark:text-gray-100">\${{ incomeCtrl.value | number: '1.2-2' }}</strong>.
            </p>
            <div class="space-y-2">
              @for (row of validRows(); track row.categoryId) {
                <div class="flex items-center justify-between p-3 rounded-lg bg-gray-50 dark:bg-gray-800/50">
                  <span class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ categoryName(row.categoryId) }}</span>
                  <div class="text-right">
                    <span class="text-sm font-bold text-indigo-600 dark:text-indigo-400">{{ row.pct }}%</span>
                    <span class="text-xs text-gray-400 ml-1">(\${{ (row.pct / 100) * incomeCtrl.value! | number: '1.0-0' }}/mo)</span>
                  </div>
                </div>
              }
            </div>
          </div>
        }
      </div>

      <div modal-footer class="flex justify-between gap-3">
        <app-button variant="ghost" (click)="step() > 1 ? step.set(step() - 1) : closed.emit()">
          {{ step() > 1 ? 'Back' : 'Cancel' }}
        </app-button>
        <div class="flex gap-2">
          @if (step() < 3) {
            <app-button variant="primary" [disabled]="!canProceed()" (click)="nextStep()">
              Next
            </app-button>
          } @else {
            <app-button variant="primary" [loading]="loading()" (click)="submit()">
              Apply Budgets
            </app-button>
          }
        </div>
      </div>
    </app-modal>
  `,
})
export class BudgetAllocationWizardComponent implements OnDestroy {
  private readonly budgetApi = inject(BudgetApiService);
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);

  readonly isOpen = input(false);
  readonly walletId = input.required<string>();
  readonly saved = output<BudgetResponse[]>();
  readonly closed = output<void>();

  protected readonly loading = signal(false);
  protected readonly step = signal(1);
  protected readonly selectedTemplate = signal<typeof PRESET_TEMPLATES[0] | null>(null);
  protected readonly allCategories = signal<CategoryResponse[]>([]);

  // Signals that mirror FormControl values so computed() can track them reactively
  private readonly _incomeVal = signal<number | null>(null);
  private readonly _allocationVersion = signal(0);
  private _rowSub = new Subscription();

  protected readonly templates = PRESET_TEMPLATES;
  protected readonly steps = [
    { n: 1, label: 'Income' },
    { n: 2, label: 'Allocations' },
    { n: 3, label: 'Review' },
  ];
  protected readonly Math = Math;

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent placeholder-gray-400 transition-colors';

  protected readonly incomeCtrl = new FormControl<number | null>(null, [Validators.required, Validators.min(1)]);

  // equal: () => false ensures the signal always notifies when .set() is called (even same reference),
  // so the @for loop re-renders when rows are added / removed.
  protected readonly allocationRows = signal(
    new FormArray<FormGroup>([this.newRow()]),
    { equal: () => false },
  );

  protected readonly expenseCategories = computed(() =>
    this.allCategories().filter(c => c.transactionType === TransactionType.EXPENSE && c.parentId === null),
  );

  protected readonly savingsCategories = computed(() =>
    this.allCategories().filter(c => c.transactionType === TransactionType.SAVINGS && c.parentId === null),
  );

  /** All root categories that can have a budget (EXPENSE + SAVINGS). */
  protected readonly budgetableCategories = computed(() => [
    ...this.expenseCategories(),
    ...this.savingsCategories(),
  ]);

  // _allocationVersion is read by totalPct / validRows so they recompute whenever
  // any form-control value inside the FormArray changes (user typing).
  protected readonly totalPct = computed(() => {
    this._allocationVersion();
    return this.allocationRows().controls.reduce((sum, r) => sum + (Number(r.value.pct) || 0), 0);
  });

  protected readonly validRows = computed(() => {
    this._allocationVersion();
    return this.allocationRows().controls
      .filter(r => r.value.categoryId && r.value.pct > 0)
      .map(r => ({ categoryId: r.value.categoryId as string, pct: Number(r.value.pct) }));
  });

  protected readonly canProceed = computed(() => {
    if (this.step() === 1) {
      const v = this._incomeVal();
      return v !== null && v > 0;
    }
    if (this.step() === 2) return this.validRows().length > 0 && this.totalPct() <= 100;
    return true;
  });

  constructor() {
    this.categoryApi.getAll().subscribe({ next: cats => this.allCategories.set(cats) });
    // Mirror income FormControl value into a signal so computed() tracks it
    this.incomeCtrl.valueChanges.subscribe(v => this._incomeVal.set(v));
    // Subscribe to FormArray value changes so totalPct / validRows recompute on keystrokes
    this._subscribeToRowChanges();
  }

  ngOnDestroy(): void {
    this._rowSub.unsubscribe();
  }

  private _subscribeToRowChanges(): void {
    this._rowSub.unsubscribe();
    this._rowSub = this.allocationRows().valueChanges.subscribe(() =>
      this._allocationVersion.update(v => v + 1),
    );
  }

  protected nextStep(): void {
    const nextN = this.step() + 1;
    if (nextN === 2) {
      const tmpl = this.selectedTemplate();
      if (tmpl) {
        const fa = new FormArray<FormGroup>([]);
        const cats = this.budgetableCategories();
        tmpl.allocations.forEach((a, i) => {
          const cat = cats[i];
          fa.push(new FormGroup({
            categoryId: new FormControl(cat?.id ?? ''),
            pct: new FormControl(a.pct),
          }));
        });
        this.allocationRows.set(fa);
        this._subscribeToRowChanges(); // re-subscribe to the new FormArray instance
      }
    }
    this.step.set(nextN);
  }

  protected addRow(): void {
    this.allocationRows().push(this.newRow());
    // trigger signal update
    this.allocationRows.set(this.allocationRows());
  }

  protected removeRow(i: number): void {
    this.allocationRows().removeAt(i);
    this.allocationRows.set(this.allocationRows());
  }

  protected categoryName(id: string): string {
    return this.allCategories().find(c => c.id === id)?.name ?? id;
  }

  protected stepCircle(n: number): string {
    const base = 'w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0';
    return this.step() === n
      ? `${base} bg-indigo-600 text-white`
      : this.step() > n
        ? `${base} bg-emerald-500 text-white`
        : `${base} bg-gray-200 dark:bg-gray-700 text-gray-500`;
  }

  protected totalBadgeClass(): string {
    const t = this.totalPct();
    const base = 'text-xs font-bold px-2 py-0.5 rounded-full';
    if (t > 100) return `${base} bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400`;
    if (t >= 90) return `${base} bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400`;
    return `${base} bg-indigo-100 dark:bg-indigo-900/30 text-indigo-700 dark:text-indigo-400`;
  }

  protected submit(): void {
    const rows = this.validRows();
    if (!rows.length || !this.incomeCtrl.value) return;
    this.loading.set(true);
    const req: BulkBudgetRequest = {
      monthlyIncome: this.incomeCtrl.value,
      allocations: rows.map(r => ({
        categoryId: r.categoryId,
        budgetType: BudgetType.PERCENTAGE,
        amount: r.pct,
      })),
    };
    this.budgetApi.setBulk(this.walletId(), req).subscribe({
      next: (budgets) => {
        this.loading.set(false);
        this.toast.success('Budget allocations applied');
        this.saved.emit(budgets);
        this.closed.emit();
        this.step.set(1);
      },
      error: (err) => {
        this.loading.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to apply budgets');
      },
    });
  }

  private newRow(): FormGroup {
    return new FormGroup({
      categoryId: new FormControl(''),
      pct: new FormControl<number | null>(null),
    });
  }
}




