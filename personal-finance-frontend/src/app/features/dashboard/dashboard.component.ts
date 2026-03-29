import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-dashboard',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Dashboard</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Your financial overview</p>
      </div>
      <app-empty-state
        title="Dashboard coming soon"
        description="Charts and analytics will be available in Phase 5."
        icon="📊"
      />
    </div>
  `,
})
export class DashboardComponent {}

