import { Component, input, output, signal, computed } from '@angular/core';
import { ClickOutsideDirective } from '../../directives/click-outside.directive';

export interface DropdownItem {
  label: string;
  value: string;
  disabled?: boolean;
  icon?: string;
}

@Component({
  selector: 'app-dropdown',
  imports: [ClickOutsideDirective],
  template: `
    <div class="relative inline-block" appClickOutside (clickOutside)="close()">
      <button
        type="button"
        [id]="triggerId"
        [attr.aria-expanded]="isOpen()"
        [attr.aria-haspopup]="true"
        (click)="toggle()"
        class="inline-flex items-center gap-2 px-3 py-2 text-sm rounded-lg border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
      >
        {{ selectedLabel() }}
        <svg class="w-4 h-4 transition-transform" [class.rotate-180]="isOpen()" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      @if (isOpen()) {
        <ul
          role="listbox"
          [attr.aria-labelledby]="triggerId"
          class="absolute z-20 mt-1 w-48 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl shadow-lg py-1 overflow-auto max-h-60"
        >
          @for (item of items(); track item.value) {
            <li
              role="option"
              [attr.aria-selected]="item.value === selectedValue()"
              [class.opacity-40]="item.disabled"
              [class.pointer-events-none]="item.disabled"
              (click)="select(item)"
              class="flex items-center gap-2 px-4 py-2 text-sm cursor-pointer text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              [class.bg-indigo-50]="item.value === selectedValue()"
              [class.dark:bg-indigo-900/20]="item.value === selectedValue()"
            >
              @if (item.icon) {
                <span>{{ item.icon }}</span>
              }
              {{ item.label }}
            </li>
          }
        </ul>
      }
    </div>
  `,
})
export class DropdownComponent {
  readonly items = input.required<DropdownItem[]>();
  readonly selectedValue = input('');
  readonly placeholder = input('Select…');

  readonly itemSelected = output<DropdownItem>();

  protected readonly isOpen = signal(false);
  protected readonly triggerId = `dropdown-${Math.random().toString(36).slice(2)}`;

  protected readonly selectedLabel = computed(() => {
    const found = this.items().find((i) => i.value === this.selectedValue());
    return found?.label ?? this.placeholder();
  });

  protected toggle(): void {
    this.isOpen.update((v) => !v);
  }

  protected close(): void {
    this.isOpen.set(false);
  }

  protected select(item: DropdownItem): void {
    if (!item.disabled) {
      this.itemSelected.emit(item);
      this.close();
    }
  }
}

