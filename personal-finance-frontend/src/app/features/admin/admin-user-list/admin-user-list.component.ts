import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-admin-user-list',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">User Management</h2>
      <app-empty-state title="Admin panel coming soon" description="User management will be available in Phase 6." icon="🛡️" />
    </div>
  `,
})
export class AdminUserListComponent {}


