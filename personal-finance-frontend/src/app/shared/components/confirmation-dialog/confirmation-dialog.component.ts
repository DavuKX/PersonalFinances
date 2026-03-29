import { Component, input, output } from '@angular/core';
import { ModalComponent } from '../modal/modal.component';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'app-confirmation-dialog',
  imports: [ModalComponent, ButtonComponent],
  template: `
    <app-modal [isOpen]="isOpen()" [title]="title()" [hasFooter]="true" (closed)="cancelled.emit()">
      <p class="text-sm text-gray-600 dark:text-gray-400">{{ message() }}</p>
      <div modal-footer class="flex justify-end gap-3">
        <app-button variant="secondary" (click)="cancelled.emit()">
          {{ cancelLabel() }}
        </app-button>
        <app-button [variant]="confirmVariant()" (click)="confirmed.emit()">
          {{ confirmLabel() }}
        </app-button>
      </div>
    </app-modal>
  `,
})
export class ConfirmationDialogComponent {
  readonly isOpen = input(false);
  readonly title = input('Are you sure?');
  readonly message = input('This action cannot be undone.');
  readonly confirmLabel = input('Confirm');
  readonly cancelLabel = input('Cancel');
  readonly confirmVariant = input<'primary' | 'danger'>('danger');

  readonly confirmed = output<void>();
  readonly cancelled = output<void>();
}

