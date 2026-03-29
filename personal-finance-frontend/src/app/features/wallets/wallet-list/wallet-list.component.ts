import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-wallet-list',
  imports: [EmptyStateComponent],
  template: `
    <div class="space-y-6">
      <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Wallets</h2>
      <app-empty-state title="Wallets coming soon" description="Wallet management will be available in Phase 3." icon="💳" />
    </div>
  `,
})
export class WalletListComponent {}


