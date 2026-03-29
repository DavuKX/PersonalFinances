import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { UserApiService } from './user-api.service';
import { UserResponse } from '../models/user.models';

const mockUser: UserResponse = {
  id: 1,
  username: 'alice',
  email: 'alice@example.com',
  roles: ['ROLE_USER'],
  createdAt: '2025-01-01T00:00:00Z',
};

describe('UserApiService', () => {
  let service: UserApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('register() posts to /api/v1/users/register', () => {
    const payload = { username: 'alice', email: 'alice@example.com', password: 'secret123' };
    service.register(payload).subscribe((r) => expect(r).toEqual(mockUser));
    const req = httpMock.expectOne('/api/v1/users/register');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    req.flush(mockUser);
  });

  it('getMe() gets /api/v1/users/me', () => {
    service.getMe().subscribe((r) => expect(r).toEqual(mockUser));
    const req = httpMock.expectOne('/api/v1/users/me');
    expect(req.request.method).toBe('GET');
    req.flush(mockUser);
  });

  it('updateProfile() puts to /api/v1/users/me', () => {
    const payload = { username: 'alice2', email: 'alice2@example.com' };
    service.updateProfile(payload).subscribe((r) => expect(r).toEqual(mockUser));
    const req = httpMock.expectOne('/api/v1/users/me');
    expect(req.request.method).toBe('PUT');
    req.flush(mockUser);
  });

  it('changePassword() patches /api/v1/users/me/password', () => {
    const payload = { currentPassword: 'old', newPassword: 'new' };
    service.changePassword(payload).subscribe();
    const req = httpMock.expectOne('/api/v1/users/me/password');
    expect(req.request.method).toBe('PATCH');
    req.flush(null);
  });
});

