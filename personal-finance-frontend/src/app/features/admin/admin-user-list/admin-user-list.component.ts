import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '../../../core/services/admin-api.service';
import { UserResponse } from '../../../core/models/user.models';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { BadgeComponent } from '../../../shared/components/badge/badge.component';
import { PaginationComponent } from '../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-admin-user-list',
  imports: [DatePipe, FormsModule, SpinnerComponent, EmptyStateComponent, BadgeComponent, PaginationComponent],
  template: `
    <div class="space-y-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">User Management</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ totalElements() }} total users</p>
        </div>
      </div>

      <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 p-4">
        <input
          type="search"
          [(ngModel)]="searchQuery"
          (ngModelChange)="onSearchChange()"
          placeholder="Search by username or email…"
          class="w-full sm:w-72 px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-400 dark:placeholder-gray-500"
        />
      </div>

      @if (loading()) {
        <div class="flex justify-center py-12"><app-spinner size="lg" /></div>
      } @else if (users().length === 0) {
        <app-empty-state
          title="No users found"
          [description]="searchQuery ? 'No users match your search.' : 'There are no registered users.'"
          icon="👥"
        />
      } @else {
        <div class="bg-white dark:bg-gray-900 rounded-xl border border-gray-200 dark:border-gray-700 overflow-hidden">
          <table class="w-full text-sm text-left">
            <thead class="bg-gray-50 dark:bg-gray-800">
              <tr>
                <th class="px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide">User</th>
                <th class="px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide hidden md:table-cell">Roles</th>
                <th class="px-6 py-3 text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wide hidden lg:table-cell">Joined</th>
                <th class="px-6 py-3"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
              @for (user of users(); track user.id) {
                <tr
                  class="hover:bg-gray-50 dark:hover:bg-gray-800/50 cursor-pointer transition-colors"
                  (click)="navigateToDetail(user.id)"
                >
                  <td class="px-6 py-4">
                    <p class="font-medium text-gray-900 dark:text-gray-100">{{ user.username }}</p>
                    <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">{{ user.email }}</p>
                  </td>
                  <td class="px-6 py-4 hidden md:table-cell">
                    <div class="flex flex-wrap gap-1">
                      @for (role of user.roles; track role) {
                        <app-badge [variant]="role === 'ROLE_ADMIN' ? 'info' : 'default'">
                          {{ role === 'ROLE_ADMIN' ? 'Admin' : 'User' }}
                        </app-badge>
                      }
                    </div>
                  </td>
                  <td class="px-6 py-4 text-gray-500 dark:text-gray-400 hidden lg:table-cell">
                    {{ user.createdAt | date: 'mediumDate' }}
                  </td>
                  <td class="px-6 py-4 text-right">
                    <span class="text-indigo-600 dark:text-indigo-400 text-xs font-medium">Manage →</span>
                  </td>
                </tr>
              }
            </tbody>
          </table>

          @if (totalPages() > 1) {
            <div class="px-6 py-4 border-t border-gray-100 dark:border-gray-800">
              <app-pagination
                [currentPage]="currentPage()"
                [totalPages]="totalPages()"
                [totalElements]="totalElements()"
                [pageSize]="pageSize"
                (pageChange)="onPageChange($event)"
              />
            </div>
          }
        </div>
      }
    </div>
  `,
})
export class AdminUserListComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  private readonly router = inject(Router);

  readonly pageSize = 15;

  readonly users = signal<UserResponse[]>([]);
  readonly currentPage = signal(1);
  readonly totalPages = signal(1);
  readonly totalElements = signal(0);
  readonly loading = signal(false);

  searchQuery = '';
  private searchTimeout: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    this.load();
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
    this.load();
  }

  onSearchChange(): void {
    if (this.searchTimeout) clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.currentPage.set(1);
      this.load();
    }, 300);
  }

  navigateToDetail(id: number): void {
    this.router.navigate(['/admin/users', id]);
  }

  private load(): void {
    this.loading.set(true);
    this.adminApi.listUsers(this.currentPage() - 1, this.pageSize, this.searchQuery).subscribe({
      next: (page) => {
        this.users.set(page.content);
        this.totalPages.set(page.totalPages);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}



