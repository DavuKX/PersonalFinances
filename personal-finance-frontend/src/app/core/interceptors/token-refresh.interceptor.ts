import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthStateService } from '../services/auth-state.service';
import { TokenStorageService } from '../services/token-storage.service';

let isRefreshing = false;
const refreshSubject$ = new BehaviorSubject<boolean>(false);

export const tokenRefreshInterceptor: HttpInterceptorFn = (req, next) => {
  const authState = inject(AuthStateService);
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status !== 401 || req.url.includes('/api/v1/auth/')) {
        return throwError(() => error);
      }

      if (isRefreshing) {
        return refreshSubject$.pipe(
          filter((done) => done),
          take(1),
          switchMap(() => next(req)),
        );
      }

      isRefreshing = true;
      refreshSubject$.next(false);

      return authState.refreshTokens().pipe(
        switchMap(() => {
          isRefreshing = false;
          refreshSubject$.next(true);
          return next(req);
        }),
        catchError((refreshError) => {
          isRefreshing = false;
          refreshSubject$.next(false);
          tokenStorage.clearTokens();
          authState.clearUser();
          router.navigate(['/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};

