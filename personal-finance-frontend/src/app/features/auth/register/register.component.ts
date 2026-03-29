import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { UserApiService } from '../../../core/services/user-api.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { passwordMatchValidator } from '../../../shared/utils/form-validators';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4 py-8">
      <div class="w-full max-w-sm">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-12 h-12 bg-indigo-600 dark:bg-indigo-500 rounded-xl mb-4">
            <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Create account</h1>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Start managing your finances today</p>
        </div>

        @if (error()) {
          <div class="mb-4 px-4 py-3 rounded-lg bg-rose-50 dark:bg-rose-900/20 border border-rose-200 dark:border-rose-800 text-sm text-rose-700 dark:text-rose-400" role="alert">
            {{ error() }}
          </div>
        }

        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
          <app-form-field label="Username" fieldId="username" [error]="fieldError('username')" [required]="true">
            <input
              id="username"
              type="text"
              formControlName="username"
              autocomplete="username"
              placeholder="your_username"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field label="Email address" fieldId="email" [error]="fieldError('email')" [required]="true">
            <input
              id="email"
              type="email"
              formControlName="email"
              autocomplete="email"
              placeholder="you@example.com"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field label="Password" fieldId="password" [error]="fieldError('password')" [required]="true">
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="new-password"
              placeholder="At least 8 characters"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field
            label="Confirm password"
            fieldId="confirmPassword"
            [error]="confirmPasswordError()"
            [required]="true"
          >
            <input
              id="confirmPassword"
              type="password"
              formControlName="confirmPassword"
              autocomplete="new-password"
              placeholder="••••••••"
              [class]="inputClass"
            />
          </app-form-field>

          <app-button
            type="submit"
            variant="primary"
            [fullWidth]="true"
            [loading]="isLoading()"
            [disabled]="form.invalid"
            size="lg"
          >
            Create account
          </app-button>
        </form>

        <p class="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
          Already have an account?
          <a routerLink="/login" class="text-indigo-600 dark:text-indigo-400 font-medium hover:underline">
            Sign in
          </a>
        </p>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private readonly userApi = inject(UserApiService);
  private readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);

  protected readonly error = signal('');
  protected readonly isLoading = signal(false);

  protected readonly form = new FormGroup(
    {
      username: new FormControl('', [Validators.required, Validators.minLength(3)]),
      email: new FormControl('', [Validators.required, Validators.email]),
      password: new FormControl('', [Validators.required, Validators.minLength(8)]),
      confirmPassword: new FormControl('', [Validators.required]),
    },
    { validators: passwordMatchValidator('password', 'confirmPassword') },
  );

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected fieldError(name: string): string {
    const ctrl = this.form.get(name)!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return `${this.capitalize(name)} is required`;
    if (ctrl.hasError('email')) return 'Enter a valid email address';
    if (ctrl.hasError('minlength')) {
      const min = ctrl.getError('minlength').requiredLength as number;
      return `Must be at least ${min} characters`;
    }
    return '';
  }

  protected confirmPasswordError(): string {
    const ctrl = this.form.get('confirmPassword')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Please confirm your password';
    if (this.form.hasError('passwordMismatch')) return 'Passwords do not match';
    return '';
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    Object.keys(this.form.controls).forEach((k) => this.form.get(k)!.markAsDirty());

    if (this.form.invalid) return;

    this.isLoading.set(true);
    this.error.set('');
    const { username, email, password } = this.form.getRawValue();

    this.userApi
      .register({ username: username!, email: email!, password: password! })
      .pipe(switchMap(() => this.authState.login({ email: email!, password: password! })))
      .subscribe({
        next: () => this.router.navigate(['/dashboard']),
        error: (err) => {
          this.isLoading.set(false);
          this.error.set(err?.error?.message ?? 'Registration failed. Please try again.');
        },
      });
  }

  private capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }
}
