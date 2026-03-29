import { Component, input, output, computed } from '@angular/core';

@Component({
  selector: 'app-pagination',
  template: `
    <div class="flex items-center justify-between gap-4 text-sm text-gray-600 dark:text-gray-400">
      <span>
        Showing {{ startItem() }}–{{ endItem() }} of {{ totalElements() }}
      </span>
      <div class="flex items-center gap-1">
        <button
          type="button"
          [disabled]="currentPage() <= 1"
          (click)="pageChange.emit(currentPage() - 1)"
          class="px-2 py-1 rounded-md border border-gray-300 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          aria-label="Previous page"
        >
          ‹
        </button>
        @for (page of visiblePages(); track page) {
          @if (page === -1) {
            <span class="px-2">…</span>
          } @else {
            <button
              type="button"
              (click)="pageChange.emit(page)"
              [class]="page === currentPage() ? activeBtnClass : inactiveBtnClass"
            >
              {{ page }}
            </button>
          }
        }
        <button
          type="button"
          [disabled]="currentPage() >= totalPages()"
          (click)="pageChange.emit(currentPage() + 1)"
          class="px-2 py-1 rounded-md border border-gray-300 dark:border-gray-600 disabled:opacity-40 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors"
          aria-label="Next page"
        >
          ›
        </button>
      </div>
    </div>
  `,
})
export class PaginationComponent {
  readonly currentPage = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly pageSize = input(10);

  readonly pageChange = output<number>();

  protected readonly activeBtnClass =
    'px-3 py-1 rounded-md bg-indigo-600 text-white dark:bg-indigo-500 font-medium';
  protected readonly inactiveBtnClass =
    'px-3 py-1 rounded-md border border-gray-300 dark:border-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors';

  readonly startItem = computed(() => (this.currentPage() - 1) * this.pageSize() + 1);
  readonly endItem = computed(() =>
    Math.min(this.currentPage() * this.pageSize(), this.totalElements()),
  );

  readonly visiblePages = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages: number[] = [1];
    if (current > 3) pages.push(-1);
    for (let p = Math.max(2, current - 1); p <= Math.min(total - 1, current + 1); p++) {
      pages.push(p);
    }
    if (current < total - 2) pages.push(-1);
    pages.push(total);
    return pages;
  });
}

