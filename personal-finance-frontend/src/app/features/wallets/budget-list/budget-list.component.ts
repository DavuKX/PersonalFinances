import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { BudgetApiService } from '../../../core/services/budget-api.service';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { BudgetSummaryResponse, BudgetType } from '../../../core/models/budget.models';
import { CategoryResponse } from '../../../core/models/category.models';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { BudgetFormDialogComponent } from '../budget-form-dialog/budget-form-dialog.component';
import { BudgetAllocationWizardComponent } from '../budget-allocation-wizard/budget-allocation-wizard.component';

@Component({
  selector: 'app-budget-list',
  imports: [
    DecimalPipe,
    ButtonComponent,
    CardComponent,
    SpinnerComponent,
    EmptyStateComponent,
    ConfirmationDialogComponent,
    BudgetFormDialogComponent,
    BudgetAllocationWizardComponent,
  ],
  template: `
    <app-card>
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">Monthly Budgets</h3>
        <div class="flex gap-2">
          <app-button size="sm" variant="ghost" (click)="wizardOpen.set(true)">🧙 Wizard</app-button>
          <app-button size="sm" variant="primary" (click)="openCreate()">+ Add Budget</app-button>
        </div>
      </div>

      @if (loading()) {
        <div class="flex justify-center py-8">
          <app-spinner />
        </div>
      } @else if (budgets().length === 0) {
        <app-empty-state
          title="No budgets yet"
          description="Set spending limits per category to track your budget vs. actual spending."
          icon="💰"
        />
      } @else {
        <div class="space-y-3 mt-2">
          @for (b of budgets(); track b.id) {
            <div class="p-4 rounded-xl border border-gray-200 dark:border-gray-700 hover:border-gray-300 dark:hover:border-gray-600 transition-colors">
              <div class="flex items-start justify-between mb-2">
                <div>
                  <p class="font-medium text-gray-900 dark:text-gray-100 text-sm">
                    {{ categoryName(b.categoryId) }}
                  </p>
                  <p class="text-xs text-gray-400 dark:text-gray-500 mt-0.5">
                    {{ b.budgetType === 'PERCENTAGE' ? b.amount + '% of income' : '' }} · Monthly
                  </p>
                </div>
                <div class="flex items-center gap-1">
                  <button class="p-1.5 text-gray-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors rounded"
                    (click)="openEdit(b)" title="Edit">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round"
                        d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  <button class="p-1.5 text-gray-400 hover:text-rose-600 dark:hover:text-rose-400 transition-colors rounded"
                    (click)="confirmDelete(b)" title="Delete">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round"
                        d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>

              <!-- Progress bar -->
              <div class="space-y-1">
                <div class="flex justify-between text-xs text-gray-500 dark:text-gray-400">
                  <span>
                    \${{ b.spentAmount | number: '1.2-2' }} spent
                    @if (b.resolvedAmount) {
                      / \${{ b.resolvedAmount | number: '1.2-2' }}
                    }
                  </span>
                  <span [class]="pctClass(b.percentUsed)">{{ b.percentUsed | number: '1.0-1' }}%</span>
                </div>
                @if (b.resolvedAmount) {
                  <div class="h-2 rounded-full bg-gray-200 dark:bg-gray-700 overflow-hidden">
                    <div class="h-full rounded-full transition-all duration-500"
                      [class]="barClass(b.percentUsed)"
                      [style.width.%]="progressWidth(b.percentUsed)">
                    </div>
                  </div>
                  @if (b.remainingAmount !== null) {
                    <p class="text-xs" [class]="b.remainingAmount >= 0 ? 'text-gray-400' : 'text-rose-500 dark:text-rose-400'">
                      @if (b.remainingAmount >= 0) {
                        \${{ b.remainingAmount | number: '1.2-2' }} remaining
                      } @else {
                        \${{ (-b.remainingAmount) | number: '1.2-2' }} over budget
                      }
                    </p>
                  }
                }
              </div>
            </div>
          }
        </div>
      }
    </app-card>

    <app-budget-form-dialog
      [isOpen]="formOpen()"
      [walletId]="walletId()"
      [editBudget]="editingBudget()"
      [existingCategoryIds]="existingCategoryIds()"
      (saved)="onSaved()"
      (closed)="formOpen.set(false); editingBudget.set(null)"
    />

    <app-budget-allocation-wizard
      [isOpen]="wizardOpen()"
      [walletId]="walletId()"
      (saved)="onWizardSaved()"
      (closed)="wizardOpen.set(false)"
    />

    <app-confirmation-dialog
      [isOpen]="deleteDialogOpen()"
      title="Delete Budget"
      [message]="deleteMessage()"
      confirmLabel="Delete"
      (confirmed)="executeDelete()"
      (cancelled)="deleteDialogOpen.set(false)"
    />
  `,
})
export class BudgetListComponent {
  private readonly budgetApi = inject(BudgetApiService);
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);

  readonly walletId = input.required<string>();

  protected readonly loading = signal(false);
  protected readonly budgets = signal<BudgetSummaryResponse[]>([]);
  protected readonly categories = signal<CategoryResponse[]>([]);

  protected readonly formOpen = signal(false);
  protected readonly wizardOpen = signal(false);
  protected readonly deleteDialogOpen = signal(false);
  protected readonly editingBudget = signal<BudgetSummaryResponse | null>(null);
  protected readonly deletingBudget = signal<BudgetSummaryResponse | null>(null);

  protected readonly existingCategoryIds = computed(() =>
    this.budgets().map(b => b.categoryId),
  );

  protected readonly deleteMessage = computed(() =>
    `Delete budget for "${this.categoryName(this.deletingBudget()?.categoryId ?? '')}"?`,
  );

  constructor() {
    effect(() => {
      const id = this.walletId();
      if (id) this.loadBudgets(id);
    });
    this.categoryApi.getAll().subscribe({ next: cats => this.categories.set(cats) });
  }

  protected categoryName(categoryId: string): string {
    return this.categories().find(c => c.id === categoryId)?.name ?? categoryId;
  }

  protected openCreate(): void {
    this.editingBudget.set(null);
    this.formOpen.set(true);
  }

  protected openEdit(b: BudgetSummaryResponse): void {
    this.editingBudget.set(b);
    this.formOpen.set(true);
  }

  protected confirmDelete(b: BudgetSummaryResponse): void {
    this.deletingBudget.set(b);
    this.deleteDialogOpen.set(true);
  }

  protected executeDelete(): void {
    const b = this.deletingBudget();
    if (!b) return;
    this.budgetApi.delete(this.walletId(), b.id).subscribe({
      next: () => {
        this.toast.success('Budget deleted');
        this.deleteDialogOpen.set(false);
        this.loadBudgets(this.walletId());
      },
      error: () => this.toast.error('Failed to delete budget'),
    });
  }

  protected onSaved(): void {
    this.loadBudgets(this.walletId());
  }

  protected onWizardSaved(): void {
    this.loadBudgets(this.walletId());
  }

  protected pctClass(pct: number): string {
    if (pct >= 100) return 'text-rose-600 dark:text-rose-400 font-medium';
    if (pct >= 90) return 'text-amber-600 dark:text-amber-400 font-medium';
    return 'text-gray-500 dark:text-gray-400';
  }

  protected barClass(pct: number): string {
    if (pct >= 100) return 'bg-rose-500';
    if (pct >= 90) return 'bg-amber-500';
    return 'bg-indigo-500';
  }

  protected progressWidth(pct: number): number {
    return Math.min(pct, 100);
  }

  private loadBudgets(walletId: string): void {
    this.loading.set(true);
    this.budgetApi.listByWallet(walletId).subscribe({
      next: (b) => { this.budgets.set(b); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}




