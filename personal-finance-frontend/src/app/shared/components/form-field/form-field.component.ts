import { Component, input, contentChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-form-field',
  template: `
    <div class="flex flex-col gap-1.5">
      @if (label()) {
        <label
          [for]="fieldId()"
          class="text-sm font-medium text-gray-700 dark:text-gray-300"
        >
          {{ label() }}
          @if (required()) {
            <span class="text-rose-500 ml-0.5" aria-hidden="true">*</span>
          }
        </label>
      }
      <ng-content />
      @if (error()) {
        <p [id]="fieldId() + '-error'" class="text-xs text-rose-600 dark:text-rose-400" role="alert">
          {{ error() }}
        </p>
      } @else if (hint()) {
        <p class="text-xs text-gray-500 dark:text-gray-400">{{ hint() }}</p>
      }
    </div>
  `,
})
export class FormFieldComponent {
  readonly label = input('');
  readonly fieldId = input('');
  readonly error = input('');
  readonly hint = input('');
  readonly required = input(false);
}

