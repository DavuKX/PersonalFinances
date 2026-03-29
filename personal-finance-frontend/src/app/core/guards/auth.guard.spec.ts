import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthStateService } from '../services/auth-state.service';

const mockRouteSnapshot = {} as ActivatedRouteSnapshot;
const mockRouterState = { url: '/dashboard' } as RouterStateSnapshot;

describe('authGuard', () => {
  let authStateMock: { isAuthenticated: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(() => {
    authStateMock = { isAuthenticated: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthStateService, useValue: authStateMock },
      ],
    });
    router = TestBed.inject(Router);
  });

  it('returns true when authenticated', () => {
    authStateMock.isAuthenticated.mockReturnValue(true);
    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRouteSnapshot, mockRouterState),
    );
    expect(result).toBe(true);
  });

  it('redirects to /login when not authenticated', () => {
    authStateMock.isAuthenticated.mockReturnValue(false);
    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRouteSnapshot, mockRouterState),
    );
    expect(result).toEqual(router.createUrlTree(['/login']));
  });
});

