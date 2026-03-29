import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminApiService } from './admin-api.service';
import { UserResponse } from '../models/user.models';

const makeUser = (id: number): UserResponse => ({
  id,
  username: `user${id}`,
  email: `user${id}@test.com`,
  roles: ['ROLE_USER'],
  createdAt: '2026-01-01T00:00:00Z',
});

describe('AdminApiService', () => {
  let service: AdminApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdminApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('listUsers sends GET to /api/v1/admin/users with page and size', () => {
    service.listUsers(0, 10).subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/admin/users');
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('listUsers sends search param when provided', () => {
    service.listUsers(0, 10, 'alice').subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/admin/users');
    expect(req.request.params.get('search')).toBe('alice');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('listUsers omits search param when empty', () => {
    service.listUsers(0, 10, '').subscribe();
    const req = httpMock.expectOne((r) => r.url === '/api/v1/admin/users');
    expect(req.request.params.has('search')).toBe(false);
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('getUser sends GET to /api/v1/admin/users/:id', () => {
    service.getUser(42).subscribe((u) => expect(u).toEqual(makeUser(42)));
    httpMock.expectOne('/api/v1/admin/users/42').flush(makeUser(42));
  });

  it('updateRoles sends PUT with roles payload', () => {
    service.updateRoles(1, { roles: ['ROLE_ADMIN'] }).subscribe();
    const req = httpMock.expectOne('/api/v1/admin/users/1/roles');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ roles: ['ROLE_ADMIN'] });
    req.flush(makeUser(1));
  });

  it('deleteUser sends DELETE to /api/v1/admin/users/:id', () => {
    service.deleteUser(5).subscribe();
    const req = httpMock.expectOne('/api/v1/admin/users/5');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});

