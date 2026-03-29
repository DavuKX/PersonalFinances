import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AdminUserListComponent } from './admin-user-list.component';
import { AdminApiService } from '../../../core/services/admin-api.service';
import { UserResponse, UserPageResponse } from '../../../core/models/user.models';

const makeUser = (id: number): UserResponse => ({
  id,
  username: `user${id}`,
  email: `user${id}@example.com`,
  roles: ['ROLE_USER'],
  createdAt: '2026-01-01T00:00:00Z',
});

const makePage = (users: UserResponse[], total = users.length): UserPageResponse => ({
  content: users,
  page: 0,
  size: 15,
  totalElements: total,
  totalPages: Math.ceil(total / 15) || 1,
});

describe('AdminUserListComponent', () => {
  let fixture: ComponentFixture<AdminUserListComponent>;
  let component: AdminUserListComponent;

  const adminApiMock = { listUsers: vi.fn() };

  function createComponent(): void {
    fixture = TestBed.createComponent(AdminUserListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.clearAllMocks();
    adminApiMock.listUsers.mockReturnValue(of(makePage([])));

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AdminApiService, useValue: adminApiMock },
      ],
    });
    createComponent();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });

  it('loads users on init', () => {
    expect(adminApiMock.listUsers).toHaveBeenCalledOnce();
  });

  it('populates users signal from API response', () => {
    const users = [makeUser(1), makeUser(2)];
    adminApiMock.listUsers.mockReturnValue(of(makePage(users, 2)));
    createComponent();
    expect(component['users']()).toHaveLength(2);
  });

  it('updates totalElements from API response', () => {
    adminApiMock.listUsers.mockReturnValue(of(makePage([makeUser(1)], 42)));
    createComponent();
    expect(component['totalElements']()).toBe(42);
  });

  it('shows empty state when no users found', () => {
    adminApiMock.listUsers.mockReturnValue(of(makePage([])));
    createComponent();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No users found');
  });

  it('renders user rows in the table', () => {
    adminApiMock.listUsers.mockReturnValue(of(makePage([makeUser(1), makeUser(2)])));
    createComponent();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('user1');
    expect(fixture.nativeElement.textContent).toContain('user2@example.com');
  });

  it('navigateToDetail navigates to /admin/users/:id', () => {
    const router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    component.navigateToDetail(7);
    expect(router.navigate).toHaveBeenCalledWith(['/admin/users', 7]);
  });

  it('onPageChange updates currentPage and reloads', () => {
    component.onPageChange(3);
    expect(component['currentPage']()).toBe(3);
    expect(adminApiMock.listUsers).toHaveBeenCalledWith(2, 15, '');
  });

  it('onSearchChange resets to page 1 and reloads after debounce', async () => {
    vi.useFakeTimers();
    component.searchQuery = 'alice';
    component.onSearchChange();
    vi.advanceTimersByTime(300);
    expect(component['currentPage']()).toBe(1);
    expect(adminApiMock.listUsers).toHaveBeenCalledWith(0, 15, 'alice');
    vi.useRealTimers();
  });

  it('loading signal is false after successful load', () => {
    expect(component['loading']()).toBe(false);
  });

  it('loading signal is false after failed load', () => {
    adminApiMock.listUsers.mockReturnValue(throwError(() => new Error()));
    createComponent();
    expect(component['loading']()).toBe(false);
  });
});

