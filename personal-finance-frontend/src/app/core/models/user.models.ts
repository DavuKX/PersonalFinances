import { PageResponse } from './pagination.models';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
}

export type UserPageResponse = PageResponse<UserResponse>;

export interface UpdateProfileRequest {
  username: string;
  email: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UpdateRolesRequest {
  roles: string[];
}

