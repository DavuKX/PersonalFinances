import { Injectable, signal, computed, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, map, of, switchMap, tap, throwError } from 'rxjs';
import { UserResponse } from '../models/user.models';
import { LoginRequest } from '../models/auth.models';
import { TokenStorageService } from './token-storage.service';
import { AuthApiService } from './auth-api.service';
import { UserApiService } from './user-api.service';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly authApi = inject(AuthApiService);
  private readonly userApi = inject(UserApiService);
  private readonly router = inject(Router);

  private readonly _currentUser = signal<UserResponse | null>(null);
  private readonly _isLoading = signal(false);

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly isAdmin = computed(() => this._currentUser()?.roles.includes('ROLE_ADMIN') ?? false);
  readonly isLoading = this._isLoading.asReadonly();

  login(request: LoginRequest): Observable<void> {
    this._isLoading.set(true);
    return this.authApi.login(request).pipe(
      switchMap((response) => {
        this.tokenStorage.saveTokens(response.accessToken, response.refreshToken);
        return this.userApi.getMe();
      }),
      tap((user) => {
        this._currentUser.set(user);
        this._isLoading.set(false);
      }),
      map(() => void 0),
      catchError((err) => {
        this._isLoading.set(false);
        return throwError(() => err);
      }),
    );
  }

  logout(): void {
    this.authApi
      .logout()
      .pipe(
        finalize(() => {
          this.tokenStorage.clearTokens();
          this._currentUser.set(null);
          this.router.navigate(['/login']);
        }),
      )
      .subscribe({ error: () => {} });
  }

  restoreSession(): Observable<UserResponse | null> {
    const token = this.tokenStorage.accessToken();
    if (!token) return of(null);

    return this.userApi.getMe().pipe(
      tap((user) => this._currentUser.set(user)),
      catchError(() => {
        this.tokenStorage.clearTokens();
        return of(null);
      }),
    );
  }

  refreshTokens(): Observable<void> {
    const refreshToken = this.tokenStorage.refreshToken();
    if (!refreshToken) return throwError(() => new Error('No refresh token available'));

    return this.authApi.refresh({ refreshToken }).pipe(
      tap((response) =>
        this.tokenStorage.saveTokens(response.accessToken, response.refreshToken),
      ),
      map(() => void 0),
    );
  }

  setUser(user: UserResponse | null): void {
    this._currentUser.set(user);
  }

  clearUser(): void {
    this._currentUser.set(null);
  }
}
