import { APP_INITIALIZER, ApplicationConfig, inject, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { API_BASE_URL } from './core/tokens/api-base-url.token';
import { apiBaseUrlInterceptor } from './core/interceptors/api-base-url.interceptor';
import { tokenRefreshInterceptor } from './core/interceptors/token-refresh.interceptor';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthStateService } from './core/services/auth-state.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(
      withInterceptors([apiBaseUrlInterceptor, tokenRefreshInterceptor, authInterceptor]),
    ),
    { provide: API_BASE_URL, useValue: 'http://localhost:8080' },
    {
      provide: APP_INITIALIZER,
      useFactory: () => {
        const authState = inject(AuthStateService);
        return () => authState.restoreSession();
      },
      multi: true,
    },
  ],
};
