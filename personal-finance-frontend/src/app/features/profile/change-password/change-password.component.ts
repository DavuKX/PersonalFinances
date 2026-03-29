import { Component } from '@angular/core';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-change-password',
  imports: [EmptyStateComponent],
  template: `<app-empty-state title="Change password coming soon" icon="🔑" />`,
})
export class ChangePasswordComponent {}


