import { Component, input, computed } from '@angular/core';

export type BadgeVariant = 'success' | 'danger' | 'warning' | 'info' | 'default';

@Component({
  selector: 'app-badge',
  template: `
    <span [class]="classes()">
      <ng-content />
    </span>
  `,
})
export class BadgeComponent {
  readonly variant = input<BadgeVariant>('default');

  readonly classes = computed(() => {
    const base = 'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium';
    const variants: Record<BadgeVariant, string> = {
      success: 'bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300',
      danger: 'bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300',
      warning: 'bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300',
      info: 'bg-blue-100 text-blue-800 dark:bg-blue-900/40 dark:text-blue-300',
      default: 'bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300',
    };
    return `${base} ${variants[this.variant()]}`;
  });
}

