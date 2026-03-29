import { Component, inject, signal } from '@angular/core';
import {
  ReactiveFormsModule,
  FormControl,
  FormGroup,
  Validators,
  AbstractControl,
  ValidationErrors,
} from '@angular/forms';
import { Router } from '@angular/router';
import { UserApiService } from '../../../core/services/user-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { CardComponent } from '../../../shared/components/card/card.component';

function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const newPw = group.get('newPassword')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return newPw && confirm && newPw !== confirm ? { passwordsMismatch: true } : null;
}

@Component({
  selector: 'app-change-password',
  imports: [ReactiveFormsModule, ButtonComponent, FormFieldComponent, CardComponent],
  template: `
    <div class="space-y-6 max-w-xl">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Change Password</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Choose a strong, unique password</p>
      </div>

      <app-card>
        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
          <app-form-field label="Current password" fieldId="currentPassword" [error]="fieldError('currentPassword')" [required]="true">
            <input
              id="currentPassword"
              type="password"
              formControlName="currentPassword"
              autocomplete="current-password"
              placeholder="••••••••"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field label="New password" fieldId="newPassword" [error]="fieldError('newPassword')" [required]="true"
            hint="At least 8 characters">
            <input
              id="newPassword"
              type="password"
              formControlName="newPassword"
              autocomplete="new-password"
              placeholder="••••••••"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field label="Confirm new password" fieldId="confirmPassword" [error]="confirmError()" [required]="true">
            <input
              id="confirmPassword"
              type="password"
              formControlName="confirmPassword"
              autocomplete="new-password"
              placeholder="••••••••"
              [class]="inputClass"
            />
          </app-form-field>

          <div class="flex items-center gap-3 pt-2">
            <app-button type="submit" variant="primary" [loading]="saving()" [disabled]="form.invalid">
              Update password
            </app-button>
            <app-button type="button" variant="ghost" (click)="cancel()">
              Cancel
            </app-button>
          </div>
        </form>
      </app-card>
    </div>
  `,
})
export class ChangePasswordComponent {
  private readonly userApi = inject(UserApiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly saving = signal(false);

  readonly form = new FormGroup(
    {
      currentPassword: new FormControl('', [Validators.required]),
      newPassword: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordsMatchValidator },
  );

  readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  submit(): void {
    this.form.markAllAsTouched();
    Object.values(this.form.controls).forEach((c) => c.markAsDirty());
    if (this.form.invalid) return;

    this.saving.set(true);
    const { currentPassword, newPassword } = this.form.getRawValue();

    this.userApi.changePassword({ currentPassword: currentPassword!, newPassword: newPassword! }).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success('Password changed successfully');
        this.router.navigate(['/profile']);
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to change password. Check your current password.');
      },
    });
  }

  cancel(): void {
    this.router.navigate(['/profile']);
  }

  fieldError(field: 'currentPassword' | 'newPassword'): string {
    const ctrl = this.form.get(field)!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'This field is required';
    if (ctrl.hasError('minlength')) return 'Password must be at least 8 characters';
    return '';
  }

  confirmError(): string {
    const ctrl = this.form.get('confirmPassword')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Please confirm your new password';
    if (this.form.hasError('passwordsMismatch')) return 'Passwords do not match';
    return '';
  }
}



