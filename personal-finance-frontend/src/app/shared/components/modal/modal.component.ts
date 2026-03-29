import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-modal',
  imports: [],
  template: `
    @if (isOpen()) {
      <div
        class="fixed inset-0 z-50 flex items-center justify-center p-4"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="titleId"
      >
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm"
          (click)="onBackdropClick()"
        ></div>
        <div
          class="relative z-10 w-full max-w-lg bg-white dark:bg-gray-900 rounded-2xl shadow-xl flex flex-col max-h-[90vh]"
        >
          <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200 dark:border-gray-700">
            <h2 [id]="titleId" class="text-lg font-semibold text-gray-900 dark:text-gray-100">
              {{ title() }}
            </h2>
            <button
              type="button"
              class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
              aria-label="Close"
              (click)="closed.emit()"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <div class="flex-1 overflow-y-auto px-6 py-4">
            <ng-content />
          </div>
          @if (hasFooter()) {
            <div class="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
              <ng-content select="[modal-footer]" />
            </div>
          }
        </div>
      </div>
    }
  `,
})
export class ModalComponent {
  readonly isOpen = input(false);
  readonly title = input('');
  readonly hasFooter = input(false);
  readonly closeOnBackdrop = input(true);

  readonly closed = output<void>();

  protected readonly titleId = `modal-title-${Math.random().toString(36).slice(2)}`;

  protected onBackdropClick(): void {
    if (this.closeOnBackdrop()) {
      this.closed.emit();
    }
  }
}


