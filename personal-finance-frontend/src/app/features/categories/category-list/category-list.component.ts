import { Component, computed, inject, signal } from '@angular/core';
import { CategoryApiService } from '../../../core/services/category-api.service';
import { CategoryResponse } from '../../../core/models/category.models';
import { TransactionType } from '../../../core/models/transaction.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { CategoryFormDialogComponent } from '../category-form-dialog/category-form-dialog.component';

interface CategoryTree {
  parent: CategoryResponse;
  children: CategoryResponse[];
}

@Component({
  selector: 'app-category-list',
  imports: [
    SpinnerComponent,
    EmptyStateComponent,
    ButtonComponent,
    BadgeComponent,
    ConfirmationDialogComponent,
    CategoryFormDialogComponent,
  ],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Categories</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Organise your income and expense categories</p>
        </div>
        <app-button variant="primary" (click)="openCreateDialog(null)">+ New Category</app-button>
      </div>

      @if (loading()) {
        <div class="flex justify-center py-12">
          <app-spinner size="lg" />
        </div>
      } @else {
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          @for (column of columns; track column.type) {
            <div>
              <div class="flex items-center justify-between mb-3">
                <h3 class="text-base font-semibold text-gray-700 dark:text-gray-300">
                  {{ column.label }}
                </h3>
                <app-button size="sm" variant="secondary" (click)="openCreateDialog(column.type)">
                  + Add
                </app-button>
              </div>

              @if (treeFor(column.type)().length === 0) {
                <app-empty-state
                  [title]="'No ' + column.label.toLowerCase() + ' categories'"
                  description="Add a category to get started."
                  icon="{{ column.icon }}"
                />
              } @else {
                <div class="space-y-2">
                  @for (node of treeFor(column.type)(); track node.parent.id) {
                    <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
                      <div class="flex items-center justify-between px-4 py-3">
                        <div class="flex items-center gap-2">
                          <span class="font-medium text-gray-900 dark:text-gray-100 text-sm">{{ node.parent.name }}</span>
                          @if (node.parent.isDefault) {
                            <app-badge variant="info">Default</app-badge>
                          }
                        </div>
                        <div class="flex items-center gap-2">
                          <app-button size="sm" variant="ghost" (click)="openCreateDialog(column.type, node.parent)">
                            + Sub
                          </app-button>
                          @if (!node.parent.isDefault) {
                            <app-button size="sm" variant="danger" (click)="confirmDelete(node.parent)">
                              Delete
                            </app-button>
                          }
                        </div>
                      </div>
                      @if (node.children.length > 0) {
                        <div class="border-t border-gray-100 dark:border-gray-800">
                          @for (child of node.children; track child.id) {
                            <div class="flex items-center justify-between px-4 py-2.5 pl-8 border-b border-gray-50 dark:border-gray-800/50 last:border-b-0 bg-gray-50/50 dark:bg-gray-800/20">
                              <div class="flex items-center gap-2">
                                <span class="text-gray-400 dark:text-gray-500 text-xs">›</span>
                                <span class="text-sm text-gray-700 dark:text-gray-300">{{ child.name }}</span>
                                @if (child.isDefault) {
                                  <app-badge variant="info">Default</app-badge>
                                }
                              </div>
                              @if (!child.isDefault) {
                                <app-button size="sm" variant="danger" (click)="confirmDelete(child)">Delete</app-button>
                              }
                            </div>
                          }
                        </div>
                      }
                    </div>
                  }
                </div>
              }
            </div>
          }
        </div>
      }
    </div>

    <app-category-form-dialog
      [isOpen]="formDialogOpen()"
      [fixedType]="dialogFixedType()"
      [availableParents]="dialogAvailableParents()"
      (saved)="onCategorySaved($event)"
      (closed)="formDialogOpen.set(false)"
    />

    <app-confirmation-dialog
      [isOpen]="deleteDialogOpen()"
      title="Delete Category"
      [message]="deleteMessage()"
      confirmLabel="Delete"
      (confirmed)="executeDelete()"
      (cancelled)="cancelDelete()"
    />
  `,
})
export class CategoryListComponent {
  private readonly categoryApi = inject(CategoryApiService);
  private readonly toast = inject(ToastService);

  protected readonly loading = signal(false);
  protected readonly categories = signal<CategoryResponse[]>([]);

  protected readonly formDialogOpen = signal(false);
  protected readonly dialogFixedType = signal<TransactionType | null>(null);
  protected readonly dialogAvailableParents = signal<CategoryResponse[]>([]);

  protected readonly deleteDialogOpen = signal(false);
  protected readonly categoryToDelete = signal<CategoryResponse | null>(null);

  protected readonly columns = [
    { type: TransactionType.INCOME, label: 'Income', icon: '💰' },
    { type: TransactionType.EXPENSE, label: 'Expense', icon: '💸' },
    { type: TransactionType.SAVINGS, label: 'Savings', icon: '🏦' },
  ];

  protected readonly deleteMessage = computed(() => {
    const name = this.categoryToDelete()?.name ?? '';
    return `Delete category "${name}"? All subcategories will also be removed.`;
  });

  constructor() {
    this.loadCategories();
  }

  private loadCategories(): void {
    this.loading.set(true);
    this.categoryApi.getAll().subscribe({
      next: (cats) => {
        this.categories.set(cats);
        this.loading.set(false);
      },
      error: () => {
        this.toast.error('Failed to load categories');
        this.loading.set(false);
      },
    });
  }

  protected treeFor(type: TransactionType): () => CategoryTree[] {
    return computed(() => {
      const filtered = this.categories().filter((c) => c.transactionType === type);
      const parents = filtered.filter((c) => c.parentId === null);
      return parents.map((parent) => ({
        parent,
        children: filtered.filter((c) => c.parentId === parent.id),
      }));
    });
  }

  protected openCreateDialog(type: TransactionType | null, parent?: CategoryResponse): void {
    this.dialogFixedType.set(type);
    this.dialogAvailableParents.set(
      parent
        ? []
        : type
          ? this.categories().filter((c) => c.transactionType === type && c.parentId === null)
          : this.categories().filter((c) => c.parentId === null),
    );
    if (parent) {
      this.dialogAvailableParents.set([parent]);
    }
    this.formDialogOpen.set(true);
  }

  protected onCategorySaved(category: CategoryResponse): void {
    this.categories.update((list) => [...list, category]);
  }

  protected confirmDelete(category: CategoryResponse): void {
    this.categoryToDelete.set(category);
    this.deleteDialogOpen.set(true);
  }

  protected cancelDelete(): void {
    this.categoryToDelete.set(null);
    this.deleteDialogOpen.set(false);
  }

  protected executeDelete(): void {
    const category = this.categoryToDelete();
    if (!category) return;
    this.categoryApi.delete(category.id).subscribe({
      next: () => {
        this.categories.update((list) =>
          list.filter((c) => c.id !== category.id && c.parentId !== category.id),
        );
        this.toast.success('Category deleted');
        this.cancelDelete();
      },
      error: () => {
        this.toast.error('Failed to delete category');
        this.cancelDelete();
      },
    });
  }
}
