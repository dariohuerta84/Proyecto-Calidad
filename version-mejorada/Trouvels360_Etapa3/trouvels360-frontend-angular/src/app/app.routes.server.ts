import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * La app depende de la sesión del usuario (cookies HttpOnly) y de datos en vivo:
 * todas las rutas se renderizan en el navegador. Así no se prerenderizan rutas con
 * parámetros (que rompían `ng build`) ni se hacen llamadas a la API desde el servidor.
 */
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
