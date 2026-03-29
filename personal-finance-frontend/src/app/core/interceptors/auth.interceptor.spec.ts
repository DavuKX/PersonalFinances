import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { TokenStorageService } from '../services/token-storage.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let tokenStorage: TokenStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    tokenStorage = TestBed.inject(TokenStorageService);
    tokenStorage.clearTokens();
  });

  afterEach(() => httpMock.verify());

  it('adds Authorization header when access token exists', () => {
    tokenStorage.saveTokens('my-token', 'my-refresh');
    http.get('/api/v1/wallets').subscribe();
    const req = httpMock.expectOne('/api/v1/wallets');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-token');
  });

  it('does not add Authorization header when no token', () => {
    http.get('/api/v1/wallets').subscribe();
    const req = httpMock.expectOne('/api/v1/wallets');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('skips Authorization for login endpoint', () => {
    tokenStorage.saveTokens('my-token', 'my-refresh');
    http.post('/api/v1/auth/login', {}).subscribe();
    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('skips Authorization for refresh endpoint', () => {
    tokenStorage.saveTokens('my-token', 'my-refresh');
    http.post('/api/v1/auth/refresh', {}).subscribe();
    const req = httpMock.expectOne('/api/v1/auth/refresh');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });

  it('skips Authorization for register endpoint', () => {
    tokenStorage.saveTokens('my-token', 'my-refresh');
    http.post('/api/v1/users/register', {}).subscribe();
    const req = httpMock.expectOne('/api/v1/users/register');
    expect(req.request.headers.has('Authorization')).toBe(false);
  });
});


