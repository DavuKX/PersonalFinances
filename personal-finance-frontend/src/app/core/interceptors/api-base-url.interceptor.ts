import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_BASE_URL } from '../tokens/api-base-url.token';

export const apiBaseUrlInterceptor: HttpInterceptorFn = (req, next) => {
  const apiBaseUrl = inject(API_BASE_URL);

  if (!req.url.startsWith('/api/')) {
    return next(req);
  }

  return next(req.clone({ url: `${apiBaseUrl}${req.url}` }));
};

