import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from '../services/token-storage.service';

const AUTH_SKIP_PATHS = ['/api/v1/auth/login', '/api/v1/auth/refresh', '/api/v1/users/register'];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.accessToken();

  const isPublic = !token || AUTH_SKIP_PATHS.some((path) => req.url.includes(path));
  if (isPublic) {
    return next(req);
  }

  return next(
    req.clone({ headers: req.headers.set('Authorization', `Bearer ${token}`) }),
  );
};

