import { Component, input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  template: `
    <div class="flex flex-col items-center justify-center py-12 text-center">
      @if (icon()) {
        <div class="text-5xl mb-4 text-gray-300 dark:text-gray-600" aria-hidden="true">
          {{ icon() }}
        </div>
      }
      <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100 mb-1">{{ title() }}</h3>
      @if (description()) {
        <p class="text-sm text-gray-500 dark:text-gray-400 max-w-sm">{{ description() }}</p>
      }
      <div class="mt-6">
        <ng-content />
      </div>
    </div>
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly description = input('');
  readonly icon = input('');
}

