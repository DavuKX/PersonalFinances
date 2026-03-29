import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { signal } from '@angular/core';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authStateMock: {
    login: ReturnType<typeof vi.fn>;
    isLoading: ReturnType<typeof signal<boolean>>;
  };

  beforeEach(async () => {
    const _isLoading = signal(false);
    authStateMock = {
      login: vi.fn(),
      isLoading: _isLoading.asReadonly() as ReturnType<typeof signal<boolean>>,
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthStateService, useValue: authStateMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  it('renders the sign-in heading', () => {
    const heading = fixture.nativeElement.querySelector('h1') as HTMLElement;
    expect(heading.textContent).toContain('Sign in');
  });

  it('has a link to /register', () => {
    const link = fixture.nativeElement.querySelector('a[routerLink="/register"]') as HTMLAnchorElement;
    expect(link).not.toBeNull();
  });

  it('form is initially invalid', () => {
    const form = (component as unknown as { form: { invalid: boolean } }).form;
    expect(form.invalid).toBe(true);
  });

  it('does not call login when form is invalid', () => {
    const btn = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    btn.click();
    expect(authStateMock.login).not.toHaveBeenCalled();
  });

  it('calls login with form values on valid submit', () => {
    authStateMock.login.mockReturnValue(of(undefined));
    fillForm('user@example.com', 'password123');
    fixture.detectChanges();
    submitForm();
    expect(authStateMock.login).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'password123',
    });
  });

  it('shows error message on login failure', () => {
    authStateMock.login.mockReturnValue(throwError(() => ({ error: { message: 'Wrong credentials' } })));
    fillForm('user@example.com', 'password123');
    fixture.detectChanges();
    submitForm();
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(alert.textContent).toContain('Wrong credentials');
  });

  it('shows fallback error message when server message is missing', () => {
    authStateMock.login.mockReturnValue(throwError(() => new Error('Network error')));
    fillForm('user@example.com', 'password123');
    fixture.detectChanges();
    submitForm();
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(alert.textContent).toContain('Invalid credentials');
  });

  function fillForm(email: string, password: string): void {
    const emailInput = fixture.nativeElement.querySelector('#email') as HTMLInputElement;
    const passwordInput = fixture.nativeElement.querySelector('#password') as HTMLInputElement;
    emailInput.value = email;
    emailInput.dispatchEvent(new Event('input'));
    passwordInput.value = password;
    passwordInput.dispatchEvent(new Event('input'));
  }

  function submitForm(): void {
    const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
    form.dispatchEvent(new Event('submit'));
  }
});



