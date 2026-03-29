import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError, firstValueFrom } from 'rxjs';
import { AuthStateService } from './auth-state.service';
import { AuthApiService } from './auth-api.service';
import { UserApiService } from './user-api.service';
import { TokenStorageService } from './token-storage.service';
import { signal } from '@angular/core';
import { UserResponse } from '../models/user.models';

const mockUser: UserResponse = {
  id: 1,
  username: 'alice',
  email: 'alice@example.com',
  roles: ['ROLE_USER'],
  createdAt: '2025-01-01T00:00:00Z',
};
const mockTokens = { accessToken: 'at', refreshToken: 'rt', expiresInSeconds: 3600 };

describe('AuthStateService', () => {
  let service: AuthStateService;

  const _accessToken = signal<string | null>(null);
  const _refreshToken = signal<string | null>(null);
  const saveTokensMock = vi.fn((at: string, rt: string) => {
    _accessToken.set(at);
    _refreshToken.set(rt);
  });
  const clearTokensMock = vi.fn(() => {
    _accessToken.set(null);
    _refreshToken.set(null);
  });

  const authApiMock = { login: vi.fn(), refresh: vi.fn(), logout: vi.fn() };
  const userApiMock = { getMe: vi.fn(), register: vi.fn() };
  const tokenStorageMock = {
    accessToken: _accessToken.asReadonly(),
    refreshToken: _refreshToken.asReadonly(),
    saveTokens: saveTokensMock,
    clearTokens: clearTokensMock,
  };

  beforeEach(() => {
    vi.clearAllMocks();
    _accessToken.set(null);
    _refreshToken.set(null);

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        { provide: AuthApiService, useValue: authApiMock },
        { provide: UserApiService, useValue: userApiMock },
        { provide: TokenStorageService, useValue: tokenStorageMock },
      ],
    });
    service = TestBed.inject(AuthStateService);
    vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('isAuthenticated starts false', () => {
    expect(service.isAuthenticated()).toBe(false);
  });

  it('isAdmin starts false', () => {
    expect(service.isAdmin()).toBe(false);
  });

  describe('login()', () => {
    it('saves tokens and sets current user on success', async () => {
      authApiMock.login.mockReturnValue(of(mockTokens));
      userApiMock.getMe.mockReturnValue(of(mockUser));

      await firstValueFrom(service.login({ email: 'alice@example.com', password: 'pass' }));

      expect(saveTokensMock).toHaveBeenCalledWith('at', 'rt');
      expect(service.currentUser()).toEqual(mockUser);
      expect(service.isAuthenticated()).toBe(true);
      expect(service.isLoading()).toBe(false);
    });

    it('clears loading state on login error', async () => {
      authApiMock.login.mockReturnValue(throwError(() => new Error('Invalid credentials')));

      await firstValueFrom(service.login({ email: 'x', password: 'y' })).catch(() => {});

      expect(service.isLoading()).toBe(false);
    });

    it('sets isAdmin for admin user', async () => {
      const adminUser = { ...mockUser, roles: ['ROLE_ADMIN'] };
      authApiMock.login.mockReturnValue(of(mockTokens));
      userApiMock.getMe.mockReturnValue(of(adminUser));

      await firstValueFrom(service.login({ email: 'admin@example.com', password: 'pass' }));

      expect(service.isAdmin()).toBe(true);
    });
  });

  describe('logout()', () => {
    it('clears user and tokens', () => {
      service.setUser(mockUser);
      authApiMock.logout.mockReturnValue(of(undefined));
      service.logout();
      expect(clearTokensMock).toHaveBeenCalled();
      expect(service.currentUser()).toBeNull();
    });

    it('clears user even when logout API fails', () => {
      service.setUser(mockUser);
      authApiMock.logout.mockReturnValue(throwError(() => new Error('network')));
      service.logout();
      expect(service.currentUser()).toBeNull();
    });
  });

  describe('restoreSession()', () => {
    it('returns null when no access token stored', async () => {
      const result = await firstValueFrom(service.restoreSession());
      expect(result).toBeNull();
    });

    it('sets current user when token is valid', async () => {
      saveTokensMock('at', 'rt');
      userApiMock.getMe.mockReturnValue(of(mockUser));

      const result = await firstValueFrom(service.restoreSession());

      expect(result).toEqual(mockUser);
      expect(service.currentUser()).toEqual(mockUser);
    });

    it('clears tokens and returns null when getMe fails', async () => {
      saveTokensMock('expired', 'rt');
      userApiMock.getMe.mockReturnValue(throwError(() => new Error('Unauthorized')));

      const result = await firstValueFrom(service.restoreSession());

      expect(result).toBeNull();
      expect(clearTokensMock).toHaveBeenCalled();
    });
  });

  describe('refreshTokens()', () => {
    it('returns error when no refresh token', async () => {
      await expect(firstValueFrom(service.refreshTokens())).rejects.toThrow(
        'No refresh token available',
      );
    });

    it('saves new tokens on success', async () => {
      saveTokensMock('old-at', 'old-rt');
      authApiMock.refresh.mockReturnValue(
        of({ accessToken: 'new-at', refreshToken: 'new-rt', expiresInSeconds: 3600 }),
      );

      await firstValueFrom(service.refreshTokens());

      expect(saveTokensMock).toHaveBeenCalledWith('new-at', 'new-rt');
    });
  });

  describe('setUser() / clearUser()', () => {
    it('setUser() updates the current user', () => {
      service.setUser(mockUser);
      expect(service.currentUser()).toEqual(mockUser);
    });

    it('clearUser() resets current user to null', () => {
      service.setUser(mockUser);
      service.clearUser();
      expect(service.currentUser()).toBeNull();
    });
  });
});



