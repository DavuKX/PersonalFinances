import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-transaction-list',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Transactions</h2>
      <app-empty-state title="Transactions coming soon" description="Transaction management will be available in Phase 4." icon="↕️" />
    </div>
  `,
})
export class TransactionListComponent {}


