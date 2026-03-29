import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { signal } from '@angular/core';
import { ProfileComponent } from './profile.component';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { UserApiService } from '../../../core/services/user-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { UserResponse } from '../../../core/models/user.models';

const mockUser: UserResponse = {
  id: 1,
  username: 'alice',
  email: 'alice@example.com',
  roles: ['ROLE_USER'],
  createdAt: '2026-01-15T00:00:00Z',
};

describe('ProfileComponent', () => {
  let fixture: ComponentFixture<ProfileComponent>;
  let component: ProfileComponent;

  const currentUserSignal = signal<UserResponse | null>(mockUser);
  const authStateMock = {
    currentUser: currentUserSignal.asReadonly(),
    setUser: vi.fn(),
  };
  const userApiMock = { updateProfile: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthStateService, useValue: authStateMock },
        { provide: UserApiService, useValue: userApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('populates form with current user data on init', () => {
    expect(component.form.value.username).toBe('alice');
    expect(component.form.value.email).toBe('alice@example.com');
  });

  it('renders username and email in inputs', () => {
    const inputs = fixture.nativeElement.querySelectorAll('input');
    const values = Array.from(inputs).map((i: any) => i.value);
    expect(values).toContain('alice');
    expect(values).toContain('alice@example.com');
  });

  it('renders role badges', () => {
    expect(fixture.nativeElement.textContent).toContain('ROLE_USER');
  });

  it('shows member since date', () => {
    expect(fixture.nativeElement.textContent).toContain('January');
  });

  it('save() calls updateProfile and updates auth state on success', () => {
    const updated = { ...mockUser, username: 'alice2' };
    userApiMock.updateProfile.mockReturnValue(of(updated));

    component.form.patchValue({ username: 'alice2' });
    component.form.markAsDirty();
    component.save();

    expect(userApiMock.updateProfile).toHaveBeenCalledWith({ username: 'alice2', email: 'alice@example.com' });
    expect(authStateMock.setUser).toHaveBeenCalledWith(updated);
    expect(toastMock.success).toHaveBeenCalled();
  });

  it('save() shows error toast on failure', () => {
    userApiMock.updateProfile.mockReturnValue(throwError(() => ({ error: { message: 'Bad request' } })));

    component.form.patchValue({ username: 'x' });
    component.form.markAsDirty();
    component.form.controls.username.setErrors(null);
    component.save();

    expect(toastMock.error).toHaveBeenCalled();
  });

  it('save() does not call API when form is invalid', () => {
    component.form.controls.email.setValue('not-an-email');
    component.form.controls.email.markAsDirty();
    component.save();
    expect(userApiMock.updateProfile).not.toHaveBeenCalled();
  });

  it('resetForm() restores original user values', () => {
    component.form.patchValue({ username: 'changed' });
    component.resetForm();
    expect(component.form.value.username).toBe('alice');
  });

  it('usernameError() returns message for too-short username', () => {
    component.form.controls.username.setValue('ab');
    component.form.controls.username.markAsDirty();
    expect(component.usernameError()).toContain('3 characters');
  });

  it('emailError() returns message for invalid email', () => {
    component.form.controls.email.setValue('bad');
    component.form.controls.email.markAsDirty();
    expect(component.emailError()).toContain('valid email');
  });
});

