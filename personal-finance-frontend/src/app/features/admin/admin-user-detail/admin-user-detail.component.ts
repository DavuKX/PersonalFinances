import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminApiService } from '../../../core/services/admin-api.service';
import { UserResponse } from '../../../core/models/user.models';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { SpinnerComponent } from '../../../shared/components/spinner/spinner.component';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { CardComponent } from '../../../shared/components/card/card.component';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';

const ALL_ROLES = ['ROLE_USER', 'ROLE_ADMIN'] as const;

@Component({
  selector: 'app-admin-user-detail',
  imports: [DatePipe, SpinnerComponent, ButtonComponent, CardComponent, ConfirmationDialogComponent],
  template: `
    @if (loading()) {
      <div class="flex justify-center py-16"><app-spinner size="xl" /></div>
    } @else if (!user()) {
      <div class="text-center py-16 text-gray-500 dark:text-gray-400">User not found.</div>
    } @else {
      <div class="space-y-6 max-w-xl">
        <div class="flex items-center gap-4">
          <button
            type="button"
            (click)="back()"
            class="text-sm text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 flex items-center gap-1"
          >
            ← Back
          </button>
          <div>
            <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">{{ user()!.username }}</h2>
            <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">{{ user()!.email }}</p>
          </div>
        </div>

        <app-card title="Account Info">
          <dl class="space-y-3 text-sm">
            <div class="flex justify-between">
              <dt class="text-gray-500 dark:text-gray-400">User ID</dt>
              <dd class="font-medium text-gray-900 dark:text-gray-100">{{ user()!.id }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-gray-500 dark:text-gray-400">Username</dt>
              <dd class="font-medium text-gray-900 dark:text-gray-100">{{ user()!.username }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-gray-500 dark:text-gray-400">Email</dt>
              <dd class="font-medium text-gray-900 dark:text-gray-100">{{ user()!.email }}</dd>
            </div>
            <div class="flex justify-between">
              <dt class="text-gray-500 dark:text-gray-400">Joined</dt>
              <dd class="font-medium text-gray-900 dark:text-gray-100">{{ user()!.createdAt | date: 'longDate' }}</dd>
            </div>
          </dl>
        </app-card>

        <app-card title="Roles">
          <div class="space-y-3">
            @for (role of allRoles; track role) {
              <label class="flex items-center gap-3 cursor-pointer select-none">
                <input
                  type="checkbox"
                  [checked]="selectedRoles().includes(role)"
                  (change)="toggleRole(role)"
                  class="rounded border-gray-300 dark:border-gray-600 text-indigo-600 focus:ring-indigo-500"
                />
                <div>
                  <p class="text-sm font-medium text-gray-900 dark:text-gray-100">{{ role }}</p>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    {{ role === 'ROLE_ADMIN' ? 'Full administrative access' : 'Standard user access' }}
                  </p>
                </div>
              </label>
            }
          </div>
          <div class="mt-4 pt-4 border-t border-gray-100 dark:border-gray-800">
            <app-button variant="primary" size="sm" [loading]="savingRoles()" (click)="saveRoles()">
              Save roles
            </app-button>
          </div>
        </app-card>

        <app-card title="Danger Zone">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-900 dark:text-gray-100">Delete user</p>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">Permanently remove this account and all its data</p>
            </div>
            <app-button variant="danger" size="sm" (click)="confirmDeleteOpen.set(true)">
              Delete
            </app-button>
          </div>
        </app-card>
      </div>
    }

    <app-confirmation-dialog
      [isOpen]="confirmDeleteOpen()"
      title="Delete user?"
      [message]="'This will permanently delete ' + (user()?.username ?? 'this user') + ' and cannot be undone.'"
      confirmLabel="Delete"
      confirmVariant="danger"
      (confirmed)="deleteUser()"
      (cancelled)="confirmDeleteOpen.set(false)"
    />
  `,
})
export class AdminUserDetailComponent implements OnInit {
  private readonly adminApi = inject(AdminApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly allRoles = ALL_ROLES;

  readonly user = signal<UserResponse | null>(null);
  readonly selectedRoles = signal<string[]>([]);
  readonly loading = signal(false);
  readonly savingRoles = signal(false);
  readonly confirmDeleteOpen = signal(false);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading.set(true);
    this.adminApi.getUser(id).subscribe({
      next: (u) => {
        this.user.set(u);
        this.selectedRoles.set([...u.roles]);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.toast.error('Failed to load user');
      },
    });
  }

  toggleRole(role: string): void {
    this.selectedRoles.update((roles) =>
      roles.includes(role) ? roles.filter((r) => r !== role) : [...roles, role],
    );
  }

  saveRoles(): void {
    this.savingRoles.set(true);
    this.adminApi.updateRoles(this.user()!.id, { roles: this.selectedRoles() }).subscribe({
      next: (updated) => {
        this.user.set(updated);
        this.savingRoles.set(false);
        this.toast.success('Roles updated');
      },
      error: (err) => {
        this.savingRoles.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to update roles');
      },
    });
  }

  deleteUser(): void {
    this.confirmDeleteOpen.set(false);
    this.adminApi.deleteUser(this.user()!.id).subscribe({
      next: () => {
        this.toast.success(`${this.user()!.username} has been deleted`);
        this.router.navigate(['/admin']);
      },
      error: (err) => this.toast.error(err?.error?.message ?? 'Failed to delete user'),
    });
  }

  back(): void {
    this.router.navigate(['/admin']);
  }
}



