import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { RoleCode } from './models';

export const roleGuard = (allowed: RoleCode[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.hasAnyRole(allowed)) {
      return true;
    }
    return router.createUrlTree(['/dashboard']);
  };
};
