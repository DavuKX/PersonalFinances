import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-admin-user-detail',
  imports: [EmptyStateComponent],
  template: `<app-empty-state title="User detail coming soon" icon="🛡️" />`,
})
export class AdminUserDetailComponent {}


