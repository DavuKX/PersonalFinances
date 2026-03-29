import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-profile',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Profile</h2>
      <app-empty-state title="Profile coming soon" description="Profile management will be available in Phase 6." icon="👤" />
    </div>
  `,
})
export class ProfileComponent {}


