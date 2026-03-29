import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ChangePasswordComponent } from './change-password.component';
import { UserApiService } from '../../../core/services/user-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';

describe('ChangePasswordComponent', () => {
  let fixture: ComponentFixture<ChangePasswordComponent>;
  let component: ChangePasswordComponent;

  const userApiMock = { changePassword: vi.fn() };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: UserApiService, useValue: userApiMock },
        { provide: ToastService, useValue: toastMock },
      ],
    });
    fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('form is initially invalid', () => {
    expect(component.form.invalid).toBe(true);
  });

  it('submit() does not call API when form is invalid', () => {
    component.submit();
    expect(userApiMock.changePassword).not.toHaveBeenCalled();
  });

  it('submit() calls changePassword with currentPassword and newPassword', () => {
    userApiMock.changePassword.mockReturnValue(of(undefined));
    fillForm('oldPass1', 'newPass123', 'newPass123');
    component.submit();
    expect(userApiMock.changePassword).toHaveBeenCalledWith({
      currentPassword: 'oldPass1',
      newPassword: 'newPass123',
    });
  });

  it('submit() shows success toast and navigates to /profile on success', () => {
    userApiMock.changePassword.mockReturnValue(of(undefined));
    const router = TestBed.inject(Router);
    fillForm('oldPass1', 'newPass123', 'newPass123');
    component.submit();
    expect(toastMock.success).toHaveBeenCalledWith('Password changed successfully');
    expect(router.navigate).toHaveBeenCalledWith(['/profile']);
  });

  it('submit() shows error toast on failure', () => {
    userApiMock.changePassword.mockReturnValue(throwError(() => ({ error: { message: 'Wrong password' } })));
    fillForm('oldPass1', 'newPass123', 'newPass123');
    component.submit();
    expect(toastMock.error).toHaveBeenCalledWith('Wrong password');
  });

  it('submit() uses fallback message when error has no message', () => {
    userApiMock.changePassword.mockReturnValue(throwError(() => ({})));
    fillForm('oldPass1', 'newPass123', 'newPass123');
    component.submit();
    expect(toastMock.error).toHaveBeenCalledWith('Failed to change password. Check your current password.');
  });

  it('form invalid when passwords do not match', () => {
    fillForm('oldPass1', 'newPass123', 'differentPass');
    expect(component.form.hasError('passwordsMismatch')).toBe(true);
  });

  it('form invalid when newPassword is too short', () => {
    fillForm('old', 'short', 'short');
    expect(component.form.controls.newPassword.hasError('minlength')).toBe(true);
  });

  it('fieldError() returns required message for empty currentPassword', () => {
    component.form.controls.currentPassword.markAsDirty();
    expect(component.fieldError('currentPassword')).toContain('required');
  });

  it('fieldError() returns minlength message for short newPassword', () => {
    component.form.controls.newPassword.setValue('short');
    component.form.controls.newPassword.markAsDirty();
    expect(component.fieldError('newPassword')).toContain('8 characters');
  });

  it('confirmError() returns mismatch message', () => {
    fillForm('old', 'newPass123', 'different');
    component.form.controls.confirmPassword.markAsDirty();
    expect(component.confirmError()).toContain('do not match');
  });

  it('confirmError() returns required message for empty confirm field', () => {
    component.form.controls.confirmPassword.markAsDirty();
    expect(component.confirmError()).toContain('confirm');
  });

  it('cancel() navigates to /profile', () => {
    const router = TestBed.inject(Router);
    component.cancel();
    expect(router.navigate).toHaveBeenCalledWith(['/profile']);
  });

  function fillForm(current: string, newPw: string, confirm: string): void {
    component.form.setValue({ currentPassword: current, newPassword: newPw, confirmPassword: confirm });
    component.form.markAllAsTouched();
    Object.values(component.form.controls).forEach((c) => c.markAsDirty());
  }
});




