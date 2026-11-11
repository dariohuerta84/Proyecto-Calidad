import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpXsrfTokenExtractor, HttpClient, HttpBackend } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, throwError, from, of } from 'rxjs';
import { catchError, switchMap, map } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { environment } from '../../../environments/environment';

const MUTANTES = ['POST', 'PUT', 'PATCH', 'DELETE'];

/**
 * Interceptor de sesión (JWT en cookie HttpOnly + CSRF de doble envío):
 *  1. Envía cookies (withCredentials) y Accept: application/json.
 *  2. En POST/PUT/PATCH/DELETE adjunta X-XSRF-TOKEN; si la cookie XSRF-TOKEN no existe o expiró,
 *     primero la pide a /auth/csrf. Si la API responde 403 por CSRF, la renueva y reintenta una vez.
 *  3. Ante 401 en rutas privadas intenta /auth/refresh una vez; si falla, limpia la sesión y
 *     lleva al login conservando la página actual (returnUrl).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const xsrf = inject(HttpXsrfTokenExtractor);
  const platformId = inject(PLATFORM_ID);
  // Cliente sin interceptores para pedir CSRF/refresh sin recursión
  const raw = new HttpClient(inject(HttpBackend));
  const api = environment.apiUrl;

  const preparar = (r: HttpRequest<unknown>, token: string | null) => {
    const headers: Record<string, string> = { Accept: 'application/json' };
    if (token && MUTANTES.includes(r.method)) headers['X-XSRF-TOKEN'] = token;
    return r.clone({ withCredentials: true, setHeaders: headers });
  };

  const pedirCsrf = (): Observable<string | null> =>
    raw.get<{ csrf_token: string }>(`${api}/auth/csrf`, { withCredentials: true }).pipe(
      map(res => xsrf.getToken() || res?.csrf_token || null),
      catchError(() => of(null))
    );

  const esMutante = MUTANTES.includes(req.method);
  const esAuth = req.url.includes('/auth/');
  const tokenInicial = xsrf.getToken();

  const enviar$ = (esMutante && !tokenInicial && isPlatformBrowser(platformId))
    ? pedirCsrf().pipe(switchMap(t => next(preparar(req, t))))
    : next(preparar(req, tokenInicial));

  return enviar$.pipe(
    catchError((error: HttpErrorResponse) => {
      // CSRF vencido o ausente → renovar y reintentar una sola vez
      const esCsrf = error.status === 403 && /csrf/i.test(error.error?.message || '');
      if (esCsrf && esMutante) {
        return pedirCsrf().pipe(switchMap(t => next(preparar(req, t))));
      }

      if (error.status === 401 && !esAuth && auth.isLoggedIn()) {
        return pedirCsrf().pipe(
          switchMap(t => raw.post(`${api}/auth/refresh`, {}, {
            withCredentials: true,
            headers: t ? { 'X-XSRF-TOKEN': t, Accept: 'application/json' } : { Accept: 'application/json' },
          })),
          switchMap(() => next(preparar(req, xsrf.getToken()))),
          catchError(() => {
            auth.cleanSession();
            if (isPlatformBrowser(platformId) && !router.url.startsWith('/auth/login')) {
              router.navigate(['/auth/login'], { queryParams: { returnUrl: router.url, expirada: 1 } });
            }
            return throwError(() => error);
          })
        );
      }
      return throwError(() => error);
    })
  );
};
