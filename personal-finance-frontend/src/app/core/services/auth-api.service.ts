import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApiService } from '../abstractions/base-api.service';
import { LoginRequest, LoginResponse, RefreshRequest } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthApiService extends BaseApiService<LoginResponse> {
  constructor() {
    super('/api/v1/auth');
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request);
  }

  refresh(request: RefreshRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/refresh`, request);
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {});
  }
}

