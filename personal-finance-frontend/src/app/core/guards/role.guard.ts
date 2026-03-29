import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from '../services/auth-state.service';

export const roleGuard = (role: string): CanActivateFn =>
  () => {
    const authState = inject(AuthStateService);
    const router = inject(Router);

    if (authState.currentUser()?.roles.includes(role)) return true;
    return router.createUrlTree(['/dashboard']);
  };

