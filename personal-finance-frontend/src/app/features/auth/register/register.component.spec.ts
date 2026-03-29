import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { UserApiService } from '../../../core/services/user-api.service';
import { AuthStateService } from '../../../core/services/auth-state.service';
import { signal } from '@angular/core';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let userApiMock: { register: ReturnType<typeof vi.fn> };
  let authStateMock: {
    login: ReturnType<typeof vi.fn>;
    isLoading: ReturnType<typeof signal<boolean>>;
  };

  beforeEach(async () => {
    const _isLoading = signal(false);
    userApiMock = { register: vi.fn() };
    authStateMock = {
      login: vi.fn(),
      isLoading: _isLoading.asReadonly() as ReturnType<typeof signal<boolean>>,
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        provideRouter([]),
        { provide: UserApiService, useValue: userApiMock },
        { provide: AuthStateService, useValue: authStateMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.detectChanges();
  });

  it('renders the create account heading', () => {
    const h1 = fixture.nativeElement.querySelector('h1') as HTMLElement;
    expect(h1.textContent).toContain('Create account');
  });

  it('has a link to /login', () => {
    const link = fixture.nativeElement.querySelector('a[routerLink="/login"]') as HTMLAnchorElement;
    expect(link).not.toBeNull();
  });

  it('does not submit when form is invalid', () => {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    expect(userApiMock.register).not.toHaveBeenCalled();
  });

  it('calls register and then login on valid submit', () => {
    const mockUser = { id: 1, username: 'alice', email: 'alice@example.com', roles: [], createdAt: '' };
    userApiMock.register.mockReturnValue(of(mockUser));
    authStateMock.login.mockReturnValue(of(undefined));

    fillForm('alice', 'alice@example.com', 'password123', 'password123');
    fixture.detectChanges();
    submitForm();

    expect(userApiMock.register).toHaveBeenCalledWith({
      username: 'alice',
      email: 'alice@example.com',
      password: 'password123',
    });
    expect(authStateMock.login).toHaveBeenCalledWith({
      email: 'alice@example.com',
      password: 'password123',
    });
  });

  it('shows error on registration failure', () => {
    userApiMock.register.mockReturnValue(
      throwError(() => ({ error: { message: 'Email already exists' } })),
    );
    fillForm('alice', 'alice@example.com', 'password123', 'password123');
    fixture.detectChanges();
    submitForm();
    fixture.detectChanges();
    const alert = fixture.nativeElement.querySelector('[role="alert"]') as HTMLElement;
    expect(alert.textContent).toContain('Email already exists');
  });

  it('shows password mismatch error when passwords differ', () => {
    fillForm('alice', 'alice@example.com', 'password123', 'different');
    fixture.detectChanges();
    submitForm();
    fixture.detectChanges();
    const text = (fixture.nativeElement as HTMLElement).textContent;
    expect(text).toContain('Passwords do not match');
  });

  function fillForm(username: string, email: string, password: string, confirm: string): void {
    setValue('#username', username);
    setValue('#email', email);
    setValue('#password', password);
    setValue('#confirmPassword', confirm);
  }

  function setValue(selector: string, value: string): void {
    const el = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    el.value = value;
    el.dispatchEvent(new Event('input'));
  }

  function submitForm(): void {
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
  }
});



