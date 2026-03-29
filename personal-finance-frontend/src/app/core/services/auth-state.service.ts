import { Injectable, signal, computed } from '@angular/core';
import { UserResponse } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class AuthStateService {
  private readonly _currentUser = signal<UserResponse | null>(null);

  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._currentUser() !== null);
  readonly isAdmin = computed(() => this._currentUser()?.roles.includes('ROLE_ADMIN') ?? false);

  setUser(user: UserResponse | null): void {
    this._currentUser.set(user);
  }

  clearUser(): void {
    this._currentUser.set(null);
  }
}

