import { Component, input } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `
    <div
      class="bg-white dark:bg-gray-900 rounded-xl shadow-sm dark:shadow-none border border-gray-200 dark:border-gray-700 overflow-hidden"
      [class.p-0]="noPadding()"
    >
      @if (title()) {
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h3 class="text-base font-semibold text-gray-900 dark:text-gray-100">{{ title() }}</h3>
        </div>
      }
      <div [class]="noPadding() ? '' : 'p-6'">
        <ng-content />
      </div>
      <ng-content select="[card-footer]" />
    </div>
  `,
})
export class CardComponent {
  readonly title = input<string>('');
  readonly noPadding = input(false);
}

