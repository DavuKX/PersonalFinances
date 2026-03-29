import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminUserDetailComponent } from './admin-user-detail.component';
import { AdminApiService } from '../../../core/services/admin-api.service';
import { ToastService } from '../../../shared/components/toast/toast.service';
import { UserResponse } from '../../../core/models/user.models';

const mockUser: UserResponse = {
  id: 42,
  username: 'alice',
  email: 'alice@example.com',
  roles: ['ROLE_USER'],
  createdAt: '2026-01-01T00:00:00Z',
};

const activatedRouteMock = {
  snapshot: { paramMap: { get: (_key: string) => '42' } },
};

describe('AdminUserDetailComponent', () => {
  let fixture: ComponentFixture<AdminUserDetailComponent>;
  let component: AdminUserDetailComponent;

  const adminApiMock = {
    getUser: vi.fn(),
    updateRoles: vi.fn(),
    deleteUser: vi.fn(),
  };
  const toastMock = { success: vi.fn(), error: vi.fn() };

  function createComponent(): void {
    fixture = TestBed.createComponent(AdminUserDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.clearAllMocks();
    adminApiMock.getUser.mockReturnValue(of(mockUser));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AdminApiService, useValue: adminApiMock },
        { provide: ToastService, useValue: toastMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    });
    createComponent();
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads user on init using route id', () => {
    expect(adminApiMock.getUser).toHaveBeenCalledWith(42);
  });

  it('user signal is populated after load', () => {
    expect(component['user']()).toEqual(mockUser);
  });

  it('selectedRoles is initialised from loaded user', () => {
    expect(component['selectedRoles']()).toEqual(['ROLE_USER']);
  });

  it('renders user username and email', () => {
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('alice');
    expect(fixture.nativeElement.textContent).toContain('alice@example.com');
  });

  it('toggleRole adds a role that is not selected', () => {
    component.toggleRole('ROLE_ADMIN');
    expect(component['selectedRoles']()).toContain('ROLE_ADMIN');
  });

  it('toggleRole removes a role that is already selected', () => {
    component.toggleRole('ROLE_USER');
    expect(component['selectedRoles']()).not.toContain('ROLE_USER');
  });

  it('saveRoles calls updateRoles with current selectedRoles', () => {
    adminApiMock.updateRoles.mockReturnValue(of({ ...mockUser, roles: ['ROLE_USER', 'ROLE_ADMIN'] }));
    component.toggleRole('ROLE_ADMIN');
    component.saveRoles();
    expect(adminApiMock.updateRoles).toHaveBeenCalledWith(42, { roles: ['ROLE_USER', 'ROLE_ADMIN'] });
  });

  it('saveRoles updates user signal and shows success toast', () => {
    const updated = { ...mockUser, roles: ['ROLE_ADMIN'] };
    adminApiMock.updateRoles.mockReturnValue(of(updated));
    component['selectedRoles'].set(['ROLE_ADMIN']);
    component.saveRoles();
    expect(component['user']()).toEqual(updated);
    expect(toastMock.success).toHaveBeenCalledWith('Roles updated');
  });

  it('saveRoles shows error toast on failure', () => {
    adminApiMock.updateRoles.mockReturnValue(throwError(() => ({ error: { message: 'Forbidden' } })));
    component.saveRoles();
    expect(toastMock.error).toHaveBeenCalledWith('Forbidden');
  });

  it('deleteUser calls adminApi.deleteUser and navigates to /admin', () => {
    adminApiMock.deleteUser.mockReturnValue(of(undefined));
    const router = TestBed.inject(Router);
    component.deleteUser();
    expect(adminApiMock.deleteUser).toHaveBeenCalledWith(42);
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
    expect(toastMock.success).toHaveBeenCalled();
  });

  it('deleteUser shows error toast on failure', () => {
    adminApiMock.deleteUser.mockReturnValue(throwError(() => ({ error: { message: 'Not found' } })));
    component.deleteUser();
    expect(toastMock.error).toHaveBeenCalledWith('Not found');
  });

  it('back() navigates to /admin', () => {
    const router = TestBed.inject(Router);
    component.back();
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
  });

  it('shows "User not found" message when user is null after load failure', () => {
    adminApiMock.getUser.mockReturnValue(throwError(() => new Error()));
    createComponent();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('User not found');
  });

  it('loading signal is false after successful load', () => {
    expect(component['loading']()).toBe(false);
  });
});




