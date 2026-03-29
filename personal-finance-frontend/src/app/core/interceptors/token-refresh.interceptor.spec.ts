import { TestBed } from '@angular/core/testing';
import { HttpClient, HttpErrorResponse, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { tokenRefreshInterceptor } from './token-refresh.interceptor';
import { AuthStateService } from '../services/auth-state.service';
import { TokenStorageService } from '../services/token-storage.service';

describe('tokenRefreshInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let authStateMock: { refreshTokens: ReturnType<typeof vi.fn>; clearUser: ReturnType<typeof vi.fn> };
  let tokenStorageMock: { clearTokens: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authStateMock = { refreshTokens: vi.fn(), clearUser: vi.fn() };
    tokenStorageMock = { clearTokens: vi.fn() };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([tokenRefreshInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: AuthStateService, useValue: authStateMock },
        { provide: TokenStorageService, useValue: tokenStorageMock },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  afterEach(() => httpMock.verify());

  it('passes through non-401 responses', () => {
    let error!: HttpErrorResponse;
    http.get('/api/v1/wallets').subscribe({ error: (e) => (error = e) });
    httpMock.expectOne('/api/v1/wallets').flush('Not Found', { status: 404, statusText: 'Not Found' });
    expect(error.status).toBe(404);
    expect(authStateMock.refreshTokens).not.toHaveBeenCalled();
  });

  it('refreshes token and retries on 401', () => {
    authStateMock.refreshTokens.mockReturnValue(of(undefined));
    let result: unknown;
    http.get('/api/v1/wallets').subscribe((r) => (result = r));

    const firstReq = httpMock.expectOne('/api/v1/wallets');
    firstReq.flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    const retryReq = httpMock.expectOne('/api/v1/wallets');
    retryReq.flush({ data: 'ok' });

    expect(authStateMock.refreshTokens).toHaveBeenCalledTimes(1);
    expect(result).toEqual({ data: 'ok' });
  });

  it('clears tokens and user on refresh failure', () => {
    authStateMock.refreshTokens.mockReturnValue(throwError(() => new Error('expired')));
    http.get('/api/v1/wallets').subscribe({ error: () => {} });

    httpMock.expectOne('/api/v1/wallets').flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });

    expect(authStateMock.clearUser).toHaveBeenCalled();
    expect(tokenStorageMock.clearTokens).toHaveBeenCalled();
  });

  it('does not retry auth endpoints on 401', () => {
    let error!: HttpErrorResponse;
    http.post('/api/v1/auth/refresh', {}).subscribe({ error: (e) => (error = e) });
    httpMock.expectOne('/api/v1/auth/refresh').flush('Unauthorized', { status: 401, statusText: 'Unauthorized' });
    expect(error.status).toBe(401);
    expect(authStateMock.refreshTokens).not.toHaveBeenCalled();
  });
});

