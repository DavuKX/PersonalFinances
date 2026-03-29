import { Component, inject, output, signal } from '@angular/core';
import { RouterLink, ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { filter, map, mergeMap } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { ThemeToggleComponent } from '../theme-toggle/theme-toggle.component';
import { AuthStateService } from '../../services/auth-state.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, ThemeToggleComponent],
  template: `
    <header class="flex items-center justify-between h-16 px-6 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-700 shrink-0">
      <div class="flex items-center gap-4">
        <button
          type="button"
          class="lg:hidden p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          aria-label="Open menu"
          (click)="menuToggled.emit()"
        >
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
        <h1 class="text-lg font-semibold text-gray-900 dark:text-gray-100">{{ pageTitle() }}</h1>
      </div>

      <div class="flex items-center gap-2">
        <app-theme-toggle />
        <div class="relative" #userMenu>
          <button
            type="button"
            (click)="userDropdownOpen.update(v => !v)"
            class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            aria-label="User menu"
          >
            <div class="w-7 h-7 rounded-full bg-indigo-100 dark:bg-indigo-900 flex items-center justify-center text-indigo-700 dark:text-indigo-300 font-medium text-xs">
              {{ userInitial() }}
            </div>
            <span class="hidden sm:block">{{ authState.currentUser()?.username ?? 'Guest' }}</span>
          </button>
          @if (userDropdownOpen()) {
            <div class="absolute right-0 mt-1 w-44 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl shadow-lg py-1 z-30">
              <a
                routerLink="/profile"
                (click)="userDropdownOpen.set(false)"
                class="block px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >Profile</a>
              <hr class="my-1 border-gray-200 dark:border-gray-700" />
              <button
                type="button"
                (click)="logout()"
                class="w-full text-left px-4 py-2 text-sm text-rose-600 dark:text-rose-400 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >Log out</button>
            </div>
          }
        </div>
      </div>
    </header>
  `,
})
export class HeaderComponent {
  protected readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);

  readonly menuToggled = output<void>();

  protected readonly userDropdownOpen = signal(false);

  protected readonly pageTitle = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => {
        let route = this.activatedRoute;
        while (route.firstChild) route = route.firstChild;
        return route;
      }),
      mergeMap((route) => route.data),
      map((data) => (data['title'] as string) ?? ''),
    ),
    { initialValue: '' },
  );

  protected readonly userInitial = () =>
    (this.authState.currentUser()?.username?.[0] ?? 'G').toUpperCase();

  protected logout(): void {
    this.userDropdownOpen.set(false);
    this.authState.clearUser();
    this.router.navigate(['/login']);
  }
}

