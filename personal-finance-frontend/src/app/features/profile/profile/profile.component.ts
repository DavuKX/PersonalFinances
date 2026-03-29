import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { UserApiService } from '../../../core/services/user-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { ButtonComponent } from '../../../shared/components/button/button.component';
import { FormFieldComponent } from '../../../shared/components/form-field/form-field.component';
import { CardComponent } from '../../../shared/components/card/card.component';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, RouterLink, ButtonComponent, FormFieldComponent, CardComponent],
  template: `
    <div class="space-y-8 max-w-xl">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-gray-100">Profile</h2>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-0.5">Manage your account information</p>
      </div>

      <app-card title="Personal Information">
        <form [formGroup]="form" (ngSubmit)="save()" class="space-y-4" novalidate>
          <app-form-field label="Username" fieldId="username" [error]="usernameError()" [required]="true">
            <input
              id="username"
              type="text"
              formControlName="username"
              autocomplete="username"
              [class]="inputClass"
            />
          </app-form-field>

          <app-form-field label="Email address" fieldId="email" [error]="emailError()" [required]="true">
            <input
              id="email"
              type="email"
              formControlName="email"
              autocomplete="email"
              [class]="inputClass"
            />
          </app-form-field>

          <div class="flex items-center gap-3 pt-2">
            <app-button type="submit" variant="primary" [loading]="saving()" [disabled]="form.invalid || form.pristine">
              Save changes
            </app-button>
            <app-button type="button" variant="ghost" (click)="resetForm()">
              Discard
            </app-button>
          </div>
        </form>
      </app-card>

      <app-card title="Security">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-gray-900 dark:text-gray-100">Password</p>
            <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">Change your current password</p>
          </div>
          <a routerLink="/profile/password">
            <app-button variant="secondary" size="sm">Change password</app-button>
          </a>
        </div>
      </app-card>

      <app-card title="Account">
        <div>
          <p class="text-xs text-gray-500 dark:text-gray-400 uppercase tracking-wide font-medium mb-1">Roles</p>
          <div class="flex flex-wrap gap-2">
            @for (role of roles(); track role) {
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800 dark:bg-indigo-900/40 dark:text-indigo-300">
                {{ role }}
              </span>
            }
          </div>
          <p class="text-xs text-gray-500 dark:text-gray-400 mt-3">
            Member since {{ createdAt() }}
          </p>
        </div>
      </app-card>
    </div>
  `,
})
export class ProfileComponent implements OnInit {
  private readonly authState = inject(AuthStateService);
  private readonly userApi = inject(UserApiService);
  private readonly toast = inject(ToastService);

  readonly saving = signal(false);

  readonly roles = computed(() => this.authState.currentUser()?.roles ?? []);
  readonly createdAt = computed(() => {
    const d = this.authState.currentUser()?.createdAt;
    return d ? new Date(d).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' }) : '';
  });

  readonly form = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(3)]),
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  readonly inputClass =
    'w-full px-3 py-2 text-sm bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 focus:border-transparent placeholder-gray-400 dark:placeholder-gray-500 transition-colors';

  ngOnInit(): void {
    this.resetForm();
  }

  resetForm(): void {
    const user = this.authState.currentUser();
    this.form.reset({ username: user?.username ?? '', email: user?.email ?? '' });
  }

  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid) return;

    this.saving.set(true);
    const { username, email } = this.form.getRawValue();

    this.userApi.updateProfile({ username: username!, email: email! }).subscribe({
      next: (user) => {
        this.authState.setUser(user);
        this.form.reset({ username: user.username, email: user.email });
        this.saving.set(false);
        this.toast.success('Profile updated successfully');
      },
      error: (err) => {
        this.saving.set(false);
        this.toast.error(err?.error?.message ?? 'Failed to update profile');
      },
    });
  }

  usernameError(): string {
    const ctrl = this.form.get('username')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Username is required';
    if (ctrl.hasError('minlength')) return 'Username must be at least 3 characters';
    return '';
  }

  emailError(): string {
    const ctrl = this.form.get('email')!;
    if (!ctrl.dirty) return '';
    if (ctrl.hasError('required')) return 'Email is required';
    if (ctrl.hasError('email')) return 'Enter a valid email address';
    return '';
  }
}



