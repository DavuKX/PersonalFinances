import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BaseApiService } from '../abstractions/base-api.service';
import { UpdateRolesRequest, UserPageResponse, UserResponse } from '../models/user.models';

@Injectable({ providedIn: 'root' })
export class AdminApiService extends BaseApiService<UserResponse> {
  constructor() {
    super('/api/v1/admin/users');
  }

  listUsers(page: number, size: number, search = ''): Observable<UserPageResponse> {
    const params: Record<string, string> = {};
    if (search.trim()) params['search'] = search.trim();
    return this.getPaged(page, size, params);
  }

  getUser(id: number): Observable<UserResponse> {
    return this.getById(id);
  }

  updateRoles(id: number, request: UpdateRolesRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.baseUrl}/${id}/roles`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.delete(id);
  }
}

