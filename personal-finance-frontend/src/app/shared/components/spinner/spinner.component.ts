import { Component, input, computed } from '@angular/core';

export type SpinnerSize = 'sm' | 'md' | 'lg' | 'xl';

@Component({
  selector: 'app-spinner',
  template: `
    <span
      role="status"
      [attr.aria-label]="label()"
      [class]="sizeClass()"
      class="inline-block rounded-full border-2 border-current border-t-transparent animate-spin text-indigo-600 dark:text-indigo-400"
    ></span>
  `,
})
export class SpinnerComponent {
  readonly size = input<SpinnerSize>('md');
  readonly label = input('Loading…');

  readonly sizeClass = computed(() => {
    const map: Record<SpinnerSize, string> = {
      sm: 'w-4 h-4',
      md: 'w-6 h-6',
      lg: 'w-8 h-8',
      xl: 'w-12 h-12',
    };
    return map[this.size()];
  });
}

