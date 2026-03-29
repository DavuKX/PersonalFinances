import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-wallet-detail',
  imports: [EmptyStateComponent],
  template: `
    <app-empty-state title="Wallet detail coming soon" icon="💳" />
  `,
})
export class WalletDetailComponent {}


