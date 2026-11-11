import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HotelService, HotelData, Habitacion } from '../../services/hoteles.service';
import { ServicioDetalleHeaderComponent, ServicioDetalleData } from '../../../../shared/components/servicio-detalle-header/servicio-detalle-header.component';
import { ReviewsSectionComponent } from '../../../../shared/components/reviews-section/reviews-section.component';
import { DateUtils } from '../../../../shared/utils/date.utils';
import { ImageUtils } from '../../../../shared/utils/image.utils';
import { ItineraryService } from '../../services/itinerary.service';
import { mensajeDeError } from '../../../../core/utils/api-error';

@Component({
  selector: 'app-detalles-hotel',
  templateUrl: './detalles-hotel.component.html',
  styleUrls: ['./detalles-hotel.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, ServicioDetalleHeaderComponent, ReviewsSectionComponent]
})
export class DetallesHotelComponent implements OnInit {
  /** Aviso en pantalla (sin alert() para que las pruebas UI no se bloqueen). */
  avisoUI = '';

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private hotelService = inject(HotelService);
  private itineraryService = inject(ItineraryService);

  hotel: HotelData | undefined;
  habitacionesFiltradas: Habitacion[] = [];
  /** Habitaciones del hotel sin filtrar por fechas */
  private habitacionesTodas: Habitacion[] = [];
  mostrarBotonReservar = false;
  cargandoDisponibilidad = false;
  errorCarga = '';
  errorItinerario = '';

  // Control de formulario emergente de fechas
  mostrarFormularioFechas = false;
  mensajeDisponibilidad = '';
  fechaMinimaHoy: string = DateUtils.getTodayISO();
  fechaMinimaCheckOut: string = '';

  // CONTROL DE MODALES DE ITINERARIO
  mostrarPreguntaItinerario = false;
  mostrarDetalleItinerario = false;
  itinerarioGenerado: any = null;
  loadingItinerario = false;

  // NUEVO: Interés seleccionado por el usuario (Default Cultura)
  interesSeleccionado: string = 'Cultura'; 
  opcionesInteres = ['Cultura', 'Aventura', 'Gastronomía', 'Relajación'];

  // Parámetros de búsqueda
  hotelId: string | null = null;
  checkInDate = '';
  checkOutDate = '';
  adultos = 1;
  ninos = 0;
  habitaciones = 1;

  get servicioData(): ServicioDetalleData | null {
    if (!this.hotel) return null;
    const galeria = ImageUtils.getAllImages(this.hotel.imagen_url, this.hotel.imagenes);
    const galeriaFinal = ImageUtils.fillGallery(galeria, 5, 'hotel');

    const imagenesApiObjects = (this.hotel as any).imagenes?.map((img: any) => {
      const url = img.url || img.imagen_url || '';
      const alt = img.alt || img.descripcion || img.caption || img.titulo || img.alt_text || '';
      return { url, alt };
    }) || [];

    const altsFinal: string[] = galeriaFinal.map(url => {
      const found = imagenesApiObjects.find((o: any) => o.url === url);
      if (found && found.alt && found.alt.trim().length > 0) return found.alt;
      return this.hotel?.nombre || '';
    });

    return {
      nombre: this.hotel.nombre,
      ciudad: this.hotel.ciudad,
      pais: this.hotel.pais,
      direccion: this.hotel.direccion,
      estrellas: this.hotel.estrellas,
      precio: this.precioHotelMostrado,
      descripcion: this.hotel.descripcion || '',
      galeria_imagenes: galeriaFinal,
      galeria_alts: altsFinal
    };
  }

  get breadcrumbItems(): string[] {
    if (!this.hotel) return [];
    return [this.hotel.ciudad, this.hotel.pais, this.hotel.nombre];
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((qParams: Record<string, any>) => {
      this.checkInDate = qParams['checkIn'] || '';
      this.checkOutDate = qParams['checkOut'] || '';
      this.adultos = +qParams['adultos'] || 1;
      this.ninos = +qParams['ninos'] || 0;
      this.habitaciones = +qParams['habitaciones'] || 1;
    });

    this.route.paramMap.subscribe((params: import('@angular/router').ParamMap) => {
      const idParam = params.get('id');
      this.hotelId = idParam;
      const hotelId = idParam ? parseInt(idParam, 10) : undefined;
      if (hotelId) this.getHotelDetails(hotelId);
    });
  }

  getHotelDetails(id: number): void {
    this.errorCarga = '';
    this.hotelService.getHotelDetalles(id).subscribe({
      next: (detalle: any) => {
        this.hotel = detalle.hotel;
        this.habitacionesTodas = (detalle.habitaciones as any[]).map((h: any) => ({ ...h, seleccionada: 0 }));
        this.habitacionesFiltradas = [...this.habitacionesTodas];
        this.verificarDisponibilidad();
      },
      error: (error: any) => {
        this.hotel = undefined;
        this.habitacionesFiltradas = [];
        this.mostrarBotonReservar = false;
        this.errorCarga = error?.status === 404 ? 'Este hotel no existe o ya no está disponible.' : mensajeDeError(error);
      }
    });
  }

  /**
   * Con fechas: consulta a la API las unidades realmente libres en todo el rango (TRV-202).
   * Sin fechas: muestra todas las habitaciones del hotel.
   */
  verificarDisponibilidad(): void {
    if (!this.hotel) return;
    if (!this.checkInDate || !this.checkOutDate || this.calcularNoches() <= 0) {
      this.mensajeDisponibilidad = '';
      this.habitacionesFiltradas = this.habitacionesTodas.map(h => ({ ...h, seleccionada: 0 }));
      this.verificarSeleccion();
      return;
    }

    this.cargandoDisponibilidad = true;
    this.hotelService.getDisponibilidad(this.hotel.id, this.checkInDate, this.checkOutDate, this.adultos, this.ninos, this.habitaciones)
      .subscribe({
        next: (habs) => {
          this.cargandoDisponibilidad = false;
          this.habitacionesFiltradas = habs.map(h => ({ ...h, seleccionada: 0 }));
          this.mensajeDisponibilidad = habs.length === 0
            ? 'No hay habitaciones disponibles para esas fechas y huéspedes. Prueba con otras fechas.'
            : '';
          this.verificarSeleccion();
        },
        error: (err) => {
          this.cargandoDisponibilidad = false;
          this.habitacionesFiltradas = [];
          this.mensajeDisponibilidad = mensajeDeError(err, 'No pudimos consultar la disponibilidad.');
          this.verificarSeleccion();
        }
      });
  }

  // ==========================================================
  // GUARDAR FECHAS
  // ==========================================================
  guardarFechas(): void {
    if (!this.checkInDate || !this.checkOutDate) {
      this.avisoUI = 'Por favor selecciona ambas fechas.';
      return;
    }
    const noches = this.calcularNoches();
    if (noches <= 0) {
      this.avisoUI = 'Las fechas no son válidas.';
      return;
    }
    this.avisoUI = '';
    this.mostrarFormularioFechas = false;
    // Las unidades libres dependen de las fechas: se recalcula y el viajero vuelve a elegir.
    this.router.navigate([], { relativeTo: this.route, queryParamsHandling: 'merge', replaceUrl: true,
      queryParams: { checkIn: this.checkInDate, checkOut: this.checkOutDate } });
    this.verificarDisponibilidad();
    this.mostrarPreguntaItinerario = false;
    setTimeout(() => this.scrollToHabitaciones(), 50);
  }

  onFechaCheckInChange(event: any): void {
    this.checkInDate = event.target.value;
    this.fechaMinimaCheckOut = DateUtils.getMinCheckoutDate(this.checkInDate);
  }

  onFechaCheckOutChange(event: any): void {
    this.checkOutDate = event.target.value;
  }

  cancelarFormularioFechas(): void {
    this.mostrarFormularioFechas = false;
  }

  get precioHotelMostrado(): number | null {
    if (!this.hotel) return null;
    if (this.hotel.precio_por_noche && this.hotel.precio_por_noche > 0)
      return this.hotel.precio_por_noche;
    const precios = this.habitacionesFiltradas.map(h => h.precio_por_noche).filter(p => p > 0);
    return precios.length > 0 ? Math.min(...precios) : null;
  }

  actualizarSeleccion(h: Habitacion, cambio: number): void {
    const limite = (h.unidades_disponibles ?? h.cantidad ?? 0);
    const prev = h.seleccionada ?? 0;
    h.seleccionada = Math.max(0, Math.min(prev + cambio, limite));
    this.verificarSeleccion();
  }

  private verificarSeleccion(): void {
    const seleccionadas = this.habitacionesFiltradas.filter(h => (h.seleccionada ?? 0) > 0);
    this.mostrarBotonReservar = seleccionadas.length > 0;
  }

  private calcularNoches(): number {
    if (!this.checkInDate || !this.checkOutDate) return 0;
    const toLocal = (s: string) => {
      const [y, m, d] = s.split('-').map(Number);
      return new Date(y, (m ?? 1) - 1, d ?? 1);
    };
    const inD = toLocal(this.checkInDate);
    const outD = toLocal(this.checkOutDate);
    const MS_DAY = 24 * 60 * 60 * 1000;
    const diff = outD.getTime() - inD.getTime();
    return diff > 0 ? Math.round(diff / MS_DAY) : 0;
  }

  volverAResultados(): void {
    try {
      const queryParams = {
        ciudad: this.hotel?.ciudad || '',
        checkIn: this.checkInDate || '',
        checkOut: this.checkOutDate || '',
        adultos: this.adultos || 1,
        ninos: this.ninos || 0,
        habitaciones: this.habitaciones || 1
      } as Record<string, any>;
      window.history.back();
      setTimeout(() => {
        const path = window.location.pathname || '';
        if (path.includes('/detalle')) {
          this.router.navigate(['/hoteles/resultados'], { queryParams });
        }
      }, 300);
    } catch (e) {
      this.router.navigate(['/hoteles/resultados']);
    }
  }

  scrollToHabitaciones(): void {
    const element = document.getElementById('seccion-habitaciones');
    if (element) element.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  // ==========================================================
  // FLUJO DE RESERVA CON ITINERARIO
  // ==========================================================

  // 1. Valida y pregunta por itinerario (CONDICIONALMENTE)
  reservarHotelFinal(vieneDeModalFechas: boolean = false): void {
    if (!this.checkInDate || !this.checkOutDate) {
      this.mostrarFormularioFechas = true;
      return;
    }
    const noches = this.calcularNoches();
    if (noches <= 0) {
      this.avisoUI = 'Por favor selecciona fechas válidas antes de continuar.';
      return;
    }
    const seleccionadas = this.habitacionesFiltradas.filter(h => (h.seleccionada ?? 0) > 0);
    if (!this.hotel || seleccionadas.length === 0) {
      this.avisoUI = 'No hay habitaciones seleccionadas.';
      return;
    }

    // 🔥 LÓGICA "SOLO EN ESE CASO"
    // Si viene del modal de fechas, mostramos la pregunta.
    // Si ya tenía fechas (clic directo), va directo a pagos sin itinerario.
    // Antes de pagar se ofrece el itinerario opcional (TRV-404); el viajero puede omitirlo.
    this.errorItinerario = '';
    this.mostrarPreguntaItinerario = true;
  }

  // Opción 2A: Rechazar itinerario
  rechazarItinerario(): void {
    this.mostrarPreguntaItinerario = false;
    
    // Limpiamos memoria para que pagos NO cobre nada extra
    if (this.hotel?.id) {
      sessionStorage.removeItem('itinerario_hotel_' + this.hotel.id);
    }
    
    // Navegamos sin itinerario
    this.navegarAPagos(false);
  }

  // Opción 2B: Aceptar itinerario -> Generar y Mostrar
  aceptarItinerario(): void {
    if (!this.hotel) return;

    this.loadingItinerario = true;

    // Preparamos los datos para Laravel
    const payload = {
      destino: this.hotel.ciudad,
      hotel_id: this.hotel.id,
      fecha_checkin: this.checkInDate,
      fecha_checkout: this.checkOutDate,
      interes: this.interesSeleccionado // Usamos el interés seleccionado
    };

    // AGREGAMOS ': any' en los parámetros response y err
    this.itineraryService.generarItinerario(payload).subscribe({
      next: (response: any) => { 
        this.loadingItinerario = false;
        this.mostrarPreguntaItinerario = false;

        // Guardamos el itinerario en la variable y en Storage
        // response.data contiene: { itinerario: [...], resumen: {...}, ... }
        this.itinerarioGenerado = response.data; 

        // Guardar en session para la vista de pagos
        const key = 'itinerario_hotel_' + this.hotel!.id;
        sessionStorage.setItem(key, JSON.stringify(this.itinerarioGenerado));
        
        // Mostrar el modal con la línea de tiempo
        this.mostrarDetalleItinerario = true;
      },
      error: (err: any) => {
        this.loadingItinerario = false;
        this.errorItinerario = mensajeDeError(err, 'No pudimos generar el itinerario.') +
          ' Puedes reintentar o continuar solo con el hotel.';
      }
    });
  }

  // Paso 3: Continuar al pago DESPUÉS de ver el itinerario
  continuarAlPagoConItinerario(): void {
    this.mostrarDetalleItinerario = false;
    this.navegarAPagos(true);
  }

  // 4. Navegación final
  private navegarAPagos(conItinerario: boolean): void {
    if (!this.hotel) return;
    const noches = this.calcularNoches();
    const seleccionadas = this.habitacionesFiltradas.filter(h => (h.seleccionada ?? 0) > 0);

    let total = 0;
    const queryParams: Record<string, any> = {
      hotelId: this.hotel.id,
      hotelNombre: this.hotel.nombre,
      ubicacion: `${this.hotel.pais}, ${this.hotel.ciudad}`,
      checkIn: this.checkInDate,
      checkOut: this.checkOutDate,
      adultos: this.adultos,
      ninos: this.ninos,
      habitaciones: this.habitaciones,
      noches,
      numTiposReservados: seleccionadas.length,
      // Flag para la página de pagos
      itinerarioBasico: conItinerario 
    };

    seleccionadas.forEach((hab, i) => {
      const cant = hab.seleccionada ?? 0;
      const precio = hab.precio_por_noche;
      const subtotal = precio * cant * noches;
      total += subtotal;

      queryParams[`reserva_${i}_tipo`] = hab.nombre;
      queryParams[`reserva_${i}_cant`] = cant;
      queryParams[`reserva_${i}_precio_unitario`] = precio;
      queryParams[`reserva_${i}_precio_total`] = subtotal.toFixed(2);
      queryParams[`reserva_${i}_habitacion_id`] = hab.id;
    });

    queryParams['precioTotalGeneral'] = total.toFixed(2);
    
    try {
      const imagenPrincipal = ImageUtils.getImageUrl(this.hotel?.imagen_url, (this.hotel as any)?.imagenes, 'hotel');
      if (imagenPrincipal) queryParams['imagen'] = imagenPrincipal;
    } catch (e) {}

    this.router.navigate(['/hoteles/pagos'], { queryParams });
  }
}