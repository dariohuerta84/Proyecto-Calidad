import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map, take, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

export const viajeroGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.currentUser$.pipe(
    take(1),
    map(user => {
      if (!user) {
        return router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
      }
      const role = String(user.rol ?? '').toLowerCase();
      return role === 'viajero' ? true : router.createUrlTree(['/']);
    }),
    catchError(err => {
      console.error('Error en viajeroGuard:', err);
      return of(router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } }));
    })
  );
};
