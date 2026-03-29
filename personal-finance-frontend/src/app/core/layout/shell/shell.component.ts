import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../sidebar/sidebar.component';
import { HeaderComponent } from '../header/header.component';
import { MobileNavComponent } from '../mobile-nav/mobile-nav.component';
import { ToastContainerComponent } from '../../../shared/components/toast/toast-container.component';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, SidebarComponent, HeaderComponent, MobileNavComponent, ToastContainerComponent],
  template: `
    <div class="flex h-screen bg-gray-50 dark:bg-gray-950 overflow-hidden">
      <div class="hidden lg:flex">
        <app-sidebar />
      </div>

      <app-mobile-nav
        [isOpen]="mobileNavOpen()"
        [navItems]="mobileNavItems"
        (closed)="mobileNavOpen.set(false)"
      />

      <div class="flex flex-col flex-1 min-w-0 overflow-hidden">
        <app-header (menuToggled)="mobileNavOpen.update(v => !v)" />
        <main class="flex-1 overflow-y-auto px-6 py-8">
          <router-outlet />
        </main>
      </div>
    </div>
    <app-toast-container />
  `,
})
export class ShellComponent {
  protected readonly mobileNavOpen = signal(false);

  protected readonly mobileNavItems = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/wallets', label: 'Wallets' },
    { path: '/transactions', label: 'Transactions' },
    { path: '/categories', label: 'Categories' },
    { path: '/analytics', label: 'Analytics' },
    { path: '/profile', label: 'Profile' },
  ];
}

