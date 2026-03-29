import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApiService } from '../abstractions/base-api.service';
import { RegisterRequest } from '../models/auth.models';
import {
  ChangePasswordRequest,
  UpdateProfileRequest,
  UserResponse,
} from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class UserApiService extends BaseApiService<UserResponse> {
  constructor() {
    super('/api/v1/users');
  }

  register(request: RegisterRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.baseUrl}/register`, request);
  }

  getMe(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.baseUrl}/me`);
  }

  updateProfile(request: UpdateProfileRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/me`, request);
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.patch<void>(`${this.baseUrl}/me/password`, request);
  }
}

