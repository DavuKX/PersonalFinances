import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-transaction-form',
  imports: [EmptyStateComponent],
  template: `<app-empty-state title="Transaction form coming soon" icon="✏️" />`,
})
export class TransactionFormComponent {}


