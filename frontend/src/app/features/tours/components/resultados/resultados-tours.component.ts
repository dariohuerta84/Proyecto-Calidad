import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { BuscadorComponent } from '../../../../shared/components/buscador/buscador.component';
import { ResultadosListaComponent, ResultadoItem } from '../../../../shared/components/resultados-lista/resultados-lista.component';
import { catchError, tap, switchMap } from 'rxjs/operators';
import { EMPTY, Observable, Subscription } from 'rxjs';
import { mensajeDeError } from '../../../../core/utils/api-error';
import { TourService, TourData } from '../../services/tour.service';
import { ServicioTransformers } from '../../../../shared/utils/transformers';

interface BusquedaTourParams {
  destino: string;
  categoria: string;
  checkIn: string;
  checkOut: string;
  personas: number;
}

@Component({
  selector: 'app-resultados-tours',
  templateUrl: './resultados-tours.component.html',
  styleUrls: ['./resultados-tours.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    BuscadorComponent,
    ResultadosListaComponent
  ]
})
export class ResultadosTOURSComponent implements OnInit, OnDestroy {

  private tourService = inject(TourService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  toursFiltrados: any[] = [];
  resultadosItems: ResultadoItem[] = [];
  totalTours: number = 0;

  // Estado de la búsqueda (HU-V08 CA-03: distinguir "sin resultados" de "error")
  cargando = true;
  error = '';
  private sub?: Subscription;
  private ultimaBusqueda: Record<string, any> = {};

  // Filtros
  destino: string = '';
  categoria: string = '';
  checkInDate: string = '';
  checkOutDate: string = '';
  personas: number = 1;

  ngOnInit(): void {
    this.cargarToursConFiltrosDeRuta();
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }

  cargarToursConFiltrosDeRuta(): void {
    // Una sola suscripción: antes se pedía dos veces (async pipe + subscribe)
    this.sub = this.route.queryParams.pipe(
      tap(params => {
        this.destino = params['destino'] || '';
        this.categoria = params['categoria'] || '';
        this.checkInDate = params['checkIn'] || '';
        this.checkOutDate = params['checkOut'] || '';
        this.personas = +params['personas'] || 1;
        this.ultimaBusqueda = params;
      }),
      switchMap(() => this.buscar()),
    ).subscribe();
  }

  private buscar(): Observable<any[]> {
    this.cargando = true;
    this.error = '';
    // 1) Si solo hay categoría y no hay checkIn → búsqueda rápida por categoría
    // 2) Si hay checkIn (obligatorio para la búsqueda avanzada) → filtros completos
    const peticion = this.categoria && !this.checkInDate
      ? this.tourService.getToursByCategoria(this.categoria)
      : this.tourService.getToursFiltrados({
          destino: this.destino,
          categoria: this.categoria,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          cupos: this.personas,
        });
    return peticion.pipe(
      tap(tours => {
        this.toursFiltrados = tours.filter((t: any) => t.tour !== null);
        this.totalTours = this.toursFiltrados.length;
        this.resultadosItems = this.transformarToursAResultados(this.toursFiltrados);
        this.cargando = false;
      }),
      catchError(e => {
        this.toursFiltrados = [];
        this.resultadosItems = [];
        this.totalTours = 0;
        this.cargando = false;
        this.error = mensajeDeError(e, 'No pudimos consultar los tours. Intenta nuevamente.');
        return EMPTY;
      }),
    );
  }

  reintentar(): void {
    this.sub?.add(this.buscar().subscribe());
  }

  /**
   * Transforma datos de tours a ResultadoItem[] para el componente genérico
   * ACTUALIZADO: Usa ServicioTransformers centralizado
   */
  private transformarToursAResultados(tours: any[]): ResultadoItem[] {
    return ServicioTransformers.toursToResultados(tours);
  }

  actualizarBusqueda(params: BusquedaTourParams): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        destino: params.destino,
        categoria: params.categoria,
        checkIn: params.checkIn,
        checkOut: params.checkOut,
        personas: params.personas
      }
    });
  }
}
