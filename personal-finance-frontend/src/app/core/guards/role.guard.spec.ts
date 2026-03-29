import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { signal } from '@angular/core';
import { roleGuard } from './role.guard';
import { AuthStateService } from '../services/auth-state.service';
import { UserResponse } from '../models/user.models';

const mockRouteSnapshot = {} as ActivatedRouteSnapshot;
const mockRouterState = { url: '/admin' } as RouterStateSnapshot;

const buildUser = (roles: string[]): UserResponse => ({
  id: 1, username: 'u', email: 'u@u.com', roles, createdAt: '',
});

describe('roleGuard', () => {
  let router: Router;
  const _user = signal<UserResponse | null>(null);
  const authStateMock = { currentUser: _user.asReadonly() };

  beforeEach(() => {
    _user.set(null);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthStateService, useValue: authStateMock },
      ],
    });
    router = TestBed.inject(Router);
  });

  it('returns true when user has the required role', () => {
    _user.set(buildUser(['ROLE_ADMIN']));
    const guard = roleGuard('ROLE_ADMIN');
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRouteSnapshot, mockRouterState),
    );
    expect(result).toBe(true);
  });

  it('redirects to /dashboard when user lacks the role', () => {
    _user.set(buildUser(['ROLE_USER']));
    const guard = roleGuard('ROLE_ADMIN');
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRouteSnapshot, mockRouterState),
    );
    expect(result).toEqual(router.createUrlTree(['/dashboard']));
  });

  it('redirects to /dashboard when user is null', () => {
    const guard = roleGuard('ROLE_ADMIN');
    const result = TestBed.runInInjectionContext(() =>
      guard(mockRouteSnapshot, mockRouterState),
    );
    expect(result).toEqual(router.createUrlTree(['/dashboard']));
  });
});
