import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { EMPTY, Subscription } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { BuscadorComponent } from '../../../../shared/components/buscador/buscador.component';
import { ResultadosListaComponent, ResultadoItem } from '../../../../shared/components/resultados-lista/resultados-lista.component';
import { HotelService } from '../../services/hoteles.service';
import { ServicioTransformers } from '../../../../shared/utils/transformers';
import { mensajeDeError } from '../../../../core/utils/api-error';

/**
 * TRV-201 — Resultados de búsqueda de hoteles.
 * Con fechas, la API devuelve solo hoteles con unidades libres todo el rango. Paginado (10 por página).
 * Distingue "sin resultados" de "error de servicio" (con Reintentar).
 */
@Component({
  selector: 'app-resultados-hoteles',
  templateUrl: './resultados-hoteles.component.html',
  styleUrls: ['./resultados-hoteles.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, BuscadorComponent, ResultadosListaComponent],
})
export class ResultadosHOTELESComponent implements OnInit, OnDestroy {
  private hotelService = inject(HotelService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private sub?: Subscription;

  resultadosItems: ResultadoItem[] = [];
  totalHoteles = 0;
  page = 1;
  lastPage = 1;
  cargando = true;
  error = '';

  ciudad = '';
  checkInDate = '';
  checkOutDate = '';
  adultos = 1;
  ninos = 0;
  habitaciones = 1;

  ngOnInit(): void {
    this.sub = this.route.queryParams.pipe(
      tap(p => {
        this.ciudad = p['ciudad'] || '';
        this.checkInDate = p['checkIn'] || '';
        this.checkOutDate = p['checkOut'] || '';
        this.adultos = +p['adultos'] || 1;
        this.ninos = +p['ninos'] || 0;
        this.habitaciones = +p['habitaciones'] || 1;
        this.page = +p['page'] || 1;
        this.cargando = true;
        this.error = '';
      }),
      switchMap(() => this.buscar()),
    ).subscribe();
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }

  private buscar() {
    const params: Record<string, any> = { page: this.page, per_page: 10 };
    if (this.ciudad) params['ciudad'] = this.ciudad;
    if (this.checkInDate && this.checkOutDate) {
      params['check_in'] = this.checkInDate;
      params['check_out'] = this.checkOutDate;
      params['adultos'] = this.adultos;
      params['ninos'] = this.ninos;
      params['habitaciones'] = this.habitaciones;
    }
    return this.hotelService.buscarHoteles(params).pipe(
      tap({
        next: r => {
          this.resultadosItems = ServicioTransformers.hotelesToResultados(r.items);
          this.totalHoteles = r.meta.total;
          this.lastPage = r.meta.last_page;
          this.cargando = false;
        },
        error: e => {
          this.resultadosItems = [];
          this.totalHoteles = 0;
          this.cargando = false;
          this.error = mensajeDeError(e, 'No pudimos consultar los hoteles. Intenta nuevamente.');
        },
      }),
      // El error ya se mostró; no se corta la suscripción a los queryParams
      catchError(() => EMPTY),
    );
  }

  reintentar(): void {
    this.cargando = true;
    this.error = '';
    this.buscar().subscribe();
  }

  irPagina(p: number): void {
    if (p < 1 || p > this.lastPage) return;
    this.router.navigate([], { relativeTo: this.route, queryParams: { page: p > 1 ? p : null }, queryParamsHandling: 'merge' });
    window.scrollTo({ top: 0 });
  }

  paginas(): number[] { return this.lastPage > 1 ? Array.from({ length: this.lastPage }, (_, i) => i + 1) : []; }
}
