import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-mobile-nav',
  imports: [RouterLink, RouterLinkActive],
  template: `
    @if (isOpen()) {
      <div class="fixed inset-0 z-40 lg:hidden">
        <div
          class="absolute inset-0 bg-black/50 backdrop-blur-sm"
          (click)="closed.emit()"
          aria-hidden="true"
        ></div>
        <nav
          class="absolute left-0 top-0 h-full w-72 bg-white dark:bg-gray-900 shadow-xl flex flex-col"
          aria-label="Mobile navigation"
        >
          <div class="flex items-center justify-between px-6 py-5 border-b border-gray-200 dark:border-gray-700">
            <span class="text-base font-semibold text-gray-900 dark:text-gray-100">Menu</span>
            <button
              type="button"
              (click)="closed.emit()"
              class="p-2 rounded-lg text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              aria-label="Close menu"
            >
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>
          <ul class="flex-1 overflow-y-auto px-3 py-4 space-y-1">
            @for (item of navItems(); track item.path) {
              <li>
                <a
                  [routerLink]="item.path"
                  routerLinkActive="bg-indigo-50 text-indigo-700 dark:bg-indigo-900/30 dark:text-indigo-300"
                  (click)="closed.emit()"
                  class="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
                >
                  {{ item.label }}
                </a>
              </li>
            }
          </ul>
        </nav>
      </div>
    }
  `,
})
export class MobileNavComponent {
  readonly isOpen = input(false);
  readonly navItems = input<{ path: string; label: string }[]>([]);

  readonly closed = output<void>();
}

