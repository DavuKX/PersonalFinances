import { Component, inject, computed } from '@angular/core';
import { ToastService, Toast, ToastType } from './toast.service';

@Component({
  selector: 'app-toast-container',
  template: `
    <div
      class="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 w-80 max-w-full pointer-events-none"
      aria-live="polite"
      aria-atomic="false"
    >
      @for (toast of toastService.toasts(); track toast.id) {
        <div
          role="alert"
          [class]="toastClass(toast.type)"
          class="pointer-events-auto flex items-start gap-3 px-4 py-3 rounded-xl shadow-lg text-sm transition-all"
        >
          <span class="shrink-0 mt-0.5">{{ icon(toast.type) }}</span>
          <span class="flex-1">{{ toast.message }}</span>
          <button
            type="button"
            class="shrink-0 opacity-70 hover:opacity-100 transition-opacity"
            aria-label="Dismiss"
            (click)="toastService.dismiss(toast.id)"
          >✕</button>
        </div>
      }
    </div>
  `,
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);

  protected toastClass(type: ToastType): string {
    const map: Record<ToastType, string> = {
      success: 'bg-emerald-50 text-emerald-900 dark:bg-emerald-900/30 dark:text-emerald-200',
      error: 'bg-rose-50 text-rose-900 dark:bg-rose-900/30 dark:text-rose-200',
      warning: 'bg-amber-50 text-amber-900 dark:bg-amber-900/30 dark:text-amber-200',
      info: 'bg-blue-50 text-blue-900 dark:bg-blue-900/30 dark:text-blue-200',
    };
    return map[type];
  }

  protected icon(type: ToastType): string {
    const map: Record<ToastType, string> = {
      success: '✓',
      error: '✕',
      warning: '⚠',
      info: 'ℹ',
    };
    return map[type];
  }
}

