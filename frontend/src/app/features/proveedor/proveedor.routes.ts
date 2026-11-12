// ==========================================================
// RUTAS DEL MÓDULO DE PROVEEDOR
// ==========================================================
// Este archivo define las rutas lazy-loaded para el módulo de proveedor.
// Incluye dashboard y formularios para crear hoteles y tours.
// TODAS las rutas están protegidas con authGuard.

import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { proveedorGuard } from '../../core/guards/proveedor.guard';

export const PROVEEDOR_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => 
      import('./components/dashboard/proveedor.component').then(m => m.ProveedorComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Panel de Proveedor - Trouvels360'
  },
  {
    path: 'crear-hotel',
    loadComponent: () => 
      import('./components/hotel-form/formulario.component').then(m => m.HotelFormComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Crear Hotel - Panel Proveedor'
  },
  {
    path: 'crear-tour',
    loadComponent: () => 
      import('./components/tour-form/formulario-tour.component').then(m => m.TourFormComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Crear Tour - Panel Proveedor'
  },
  {
    path: 'editar-hotel/:id',
    loadComponent: () => 
      import('./components/editar-hotel/editar-hotel.component').then(m => m.EditarHotelComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Editar Hotel - Panel Proveedor'
  },
  {
    path: 'editar-tour/:id',
    loadComponent: () => 
      import('./components/editar-tour/editar-tour.component').then(m => m.EditarTourComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Editar Tour - Panel Proveedor'
  },
  {
    path: 'reservas',
    loadComponent: () =>
      import('./components/reservas-recibidas/reservas-recibidas.component').then(m => m.ReservasRecibidasComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Reservas recibidas - Panel Proveedor'
  },
  {
    path: 'tour/:id/programa',
    loadComponent: () =>
      import('./components/gestionar-tour/gestionar-tour.component').then(m => m.GestionarTourComponent),
    canActivate: [authGuard, proveedorGuard],
    title: 'Salidas y actividades - Panel Proveedor'
  }
];
