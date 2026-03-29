import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-analytics-overview',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Analytics</h2>
      <app-empty-state title="Analytics coming soon" description="Charts and trend data will be available in Phase 5." icon="📈" />
    </div>
  `,
})
export class AnalyticsOverviewComponent {}


