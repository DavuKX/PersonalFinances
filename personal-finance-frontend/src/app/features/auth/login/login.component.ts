import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-950 px-4">
      <div class="w-full max-w-sm">

        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-12 h-12 bg-indigo-600 dark:bg-indigo-500 rounded-xl mb-4">
            <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h1 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Sign in</h1>
          <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">Welcome back to PersonalFinance</p>
        </div>

        @if (error()) {
          <div class="mb-4 px-4 py-3 rounded-lg bg-rose-50 dark:bg-rose-900/20 border border-rose-200 dark:border-rose-800 text-sm text-rose-700 dark:text-rose-400" role="alert">
            {{ error() }}
          </div>
        }

        <form [formGroup]="form" (ngSubmit)="submit()" class="space-y-4" novalidate>
          <app-form-field
            label="Email address"
            fieldId="email"
            [error]="emailError()"
            [required]="true"
          >
            <input
              id="email"
              type="email"
              formControlName="email"
              autocomplete="email"
              placeholder="you@example.com"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field
            label="Password"
            fieldId="password"
            [error]="passwordError()"
            [required]="true"
          >
            <input
              id="password"
              type="password"
              formControlName="password"
              autocomplete="current-password"
              placeholder="••••••••"
              [class]="inputClass"
            />
          </app-form-field>

          <app-button
            type="submit"
            variant="primary"
            [fullWidth]="true"
            [loading]="authState.isLoading()"
            [disabled]="form.invalid"
            size="lg"
          >
            Sign in
          </app-button>
        </form>

        <p class="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
          Don't have an account?
          <a routerLink="/register" class="text-indigo-600 dark:text-indigo-400 font-medium hover:underline">
            Create one
          </a>
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  protected readonly authState = inject(AuthStateService);
  private readonly router = inject(Router);

  protected readonly error = signal('');

  protected readonly form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
  });

  protected readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  protected emailError(): string {
    const ctrl = this.form.get('email')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Email is required';
    if (ctrl.hasError('email')) return 'Enter a valid email address';
    return '';
  }

  protected passwordError(): string {
    const ctrl = this.form.get('password')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Password is required';
    if (ctrl.hasError('minlength')) return 'Password must be at least 6 characters';
    return '';
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    this.form.controls.email.markAsDirty();
    this.form.controls.password.markAsDirty();

    if (this.form.invalid) return;

    this.error.set('');
    const { email, password } = this.form.getRawValue();

    this.authState.login({ email: email!, password: password! }).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Invalid credentials. Please try again.');
      },
    });
  }
}
