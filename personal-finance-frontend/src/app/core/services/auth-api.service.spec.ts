import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthApiService } from './auth-api.service';

describe('AuthApiService', () => {
  let service: AuthApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login() posts to /api/v1/auth/login', () => {
    const mockResp = { accessToken: 'at', refreshToken: 'rt', expiresInSeconds: 3600 };
    service.login({ email: 'a@b.com', password: 'pass' }).subscribe((r) => {
      expect(r).toEqual(mockResp);
    });
    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'a@b.com', password: 'pass' });
    req.flush(mockResp);
  });

  it('refresh() posts to /api/v1/auth/refresh', () => {
    const mockResp = { accessToken: 'at2', refreshToken: 'rt2', expiresInSeconds: 3600 };
    service.refresh({ refreshToken: 'rt' }).subscribe((r) => expect(r).toEqual(mockResp));
    const req = httpMock.expectOne('/api/v1/auth/refresh');
    expect(req.request.method).toBe('POST');
    req.flush(mockResp);
  });

  it('logout() posts to /api/v1/auth/logout', () => {
    service.logout().subscribe();
    const req = httpMock.expectOne('/api/v1/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush(null);
  });
});

