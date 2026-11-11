import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  ReactiveFormsModule,
  FormControl,
} from '@angular/forms';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { HotelService, HotelDetalles, HabitacionCreatePayload, HotelCreatePayload,HabitacionUpdatePayload } from '../../../hoteles/services/hoteles.service'; // Importar interfaces
import { AuthService } from '../../../../core/services/auth.service';

/**
 * Componente dedicado exclusivamente a la creación/edición de Hoteles.
 * Implementa la validación de roles en el envío.
 */
import { mensajeDeError, erroresPorCampo } from '../../../../core/utils/api-error';

@Component({
  selector: 'app-editar-hotel', 
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editar-hotel.component.html',
  styleUrls: ['./editar-hotel.component.css'],
})
export class EditarHotelComponent implements OnInit {
  // ======================================================
  // 🧩 Variables principales

  // ======================================================
  hotelId: number | null = null;
  cargandoDatosHotel: boolean = true;
  hotelForm!: FormGroup;
  enviado = false;
  enviando = false;
  mensajeExito = '';
  mensajeError = '';

  // Limite para la descripción de cada habitación (500 caracteres)
  readonly MAX_DESCRIPCION_HAB = 500;
  // Contadores individuales para las descripciones de las habitaciones
  habitacionesDescripcionLengths: number[] = [];

  // ======================================================
  // 🧱 Inyección de dependencias
  // ======================================================
  private fb = inject(FormBuilder);
  private hotelService = inject(HotelService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  // ======================================================
  // 🚀 Inicialización
  // ======================================================
  ngOnInit(): void {
    this.crearFormulario();
    this.route.paramMap.subscribe({
      next: (params: ParamMap) => {
        const idString = params.get('id'); // 'id' debe coincidir con el nombre de tu ruta: 'editar-tour/:id'
        
        if (idString) {
          // El '+' convierte el string del parámetro a número (number)
          this.hotelId = +idString; 
          console.log('ID del Hotel extraído:', this.hotelId);
          
          // 3. Llamar a la función de carga
          this.cargarDatosHotel(this.hotelId);
        } else {
          console.error('No se encontró el ID del tour en la ruta.');
        }
      },
      error: (err) => {
        console.error('Error al leer los parámetros de la ruta:', err);
      }
    });
    // Inicializar contadores por habitación
    this.habitacionesDescripcionLengths = this.habitaciones.controls.map(() => 0);
  }

  // ======================================================
  // 🏗️ Construcción del formulario
  // ======================================================
  crearFormulario(): void {
    this.hotelForm = this.fb.group({
      // Agrupación para los datos del Servicio/Hotel (Payload 1)
      hotel: this.fb.group({
        nombre: ['', [Validators.required]],
        descripcion: ['', [Validators.required, Validators.minLength(10)]],
        direccion: ['', [Validators.required]],
        ciudad: ['', [Validators.required]],
        pais: ['', [Validators.required]],
        estrellas: [3, [Validators.required, Validators.min(1), Validators.max(5)]], 
        imagen_url: ['', [Validators.required]],
        // Inicializamos la galería con un solo campo de control vacío por defecto
        imagenes: this.fb.array<FormGroup>([]), 
      }),
      // Agrupación para las Habitaciones (Payload 2, se procesa internamente)
      habitaciones: this.fb.array<FormGroup>([this.crearHabitacion()]),
    });
  }

  // ======================================================
  // 🧾 Getters para acceder fácilmente a los campos
  // ======================================================
  get hotelGroup(): FormGroup {
    return this.hotelForm.get('hotel') as FormGroup;
  }

  get galeriaImagenes(): FormArray<FormGroup> {
    return this.hotelGroup.get('imagenes') as FormArray<FormGroup>;
  }

  get habitaciones(): FormArray<FormGroup> {
    return this.hotelForm.get('habitaciones') as FormArray<FormGroup>;
  }

  // ======================================================
  // 🖼️ Lógica de Galería de Imágenes
  // ======================================================
  agregarImagen(url: string, alt:string=''): void {
    const MAX_IMAGES = 5; // Definimos el límite máximo de imágenes
  
    // 1. Verificación de límite máximo
    if (this.galeriaImagenes.length >= MAX_IMAGES) {
      console.warn(`[GALERIA] Límite de ${MAX_IMAGES} imágenes alcanzado. No se agregará la URL.`);
      // Opcional: podrías mostrar un mensaje de error al usuario aquí
      return;
    }
    
    // Acepta el argumento 'url' enviado desde el template, resolviendo el error de argumentos.
    // Solo agrega el control si la URL no está vacía.
    if (url && url.trim().length > 0) {
      const imagenGroup = this.fb.group({
        url: [url, [Validators.required]],
        alt: [alt], // opcional
      });
      this.galeriaImagenes.push(imagenGroup);
      console.log(`[GALERIA] Imagen agregada. Total: ${this.galeriaImagenes.length}/${MAX_IMAGES}`);
    }
  }
  
  eliminarImagen(index: number): void {
    // Elimina el campo de imagen en el índice dado
    this.galeriaImagenes.removeAt(index);
    console.log(`[GALERIA] Imagen eliminada. Total: ${this.galeriaImagenes.length}/5`);
  }

  // ======================================================
  // 🏨 Lógica de Habitaciones
  // ======================================================
  crearHabitacion(): FormGroup {
    return this.fb.group({
      id:[null],
      nombre: ['', [Validators.required]],
      capacidad_adultos: [1, [Validators.required, Validators.min(1)]],
      capacidad_ninos: [0, [Validators.min(0)]],
      // [FIX]: Cambiamos el valor inicial de 0 a 100 para que cumpla con Validators.min(1) 
      // y no bloquee el formulario al inicio.
      precio_por_noche: [100, [Validators.required, Validators.min(1)]], 
      cantidad: [1, [Validators.required, Validators.min(1)]],
      descripcion: ['', [Validators.maxLength(this.MAX_DESCRIPCION_HAB)]],
    });
  }

  agregarHabitacion(): void {
    this.habitaciones.push(this.crearHabitacion());
    this.habitacionesDescripcionLengths.push(0);
  }

  eliminarHabitacion(index: number): void {
    if (this.habitaciones.length > 1) { 
      this.habitaciones.removeAt(index);
      this.habitacionesDescripcionLengths.splice(index, 1);
    }
  }
  
  // Maneja el input de la descripción de una habitación específica
  onHabitacionDescripcionInput(event: any, index: number): void {
    const raw = event?.target?.value ?? '';
    if (raw.length > this.MAX_DESCRIPCION_HAB) {
      const truncated = raw.slice(0, this.MAX_DESCRIPCION_HAB);
      const ctrl = this.habitaciones.at(index).get('descripcion') as FormControl;
      if (ctrl) {
        ctrl.setValue(truncated, { emitEvent: false });
      }
      this.habitacionesDescripcionLengths[index] = this.MAX_DESCRIPCION_HAB;
    } else {
      this.habitacionesDescripcionLengths[index] = raw.length;
    }
  }
  // ======================================================
  // Cargar datos de hotel
  // ======================================================
  cargarDatosHotel(id: number): void {
    this.cargandoDatosHotel = true;
    // 1. Llamar al servicio para obtener los datos
    this.hotelService.getHotelDetalles(id).subscribe({
      next: (data:HotelDetalles) => {
        console.log('Datos del Hotel cargados:', data);
        // 2. Precargar el formulario principal (Hotel Group)
        this.hotelForm.patchValue({
          hotel: {
            nombre: data.hotel.nombre,
            descripcion: data.hotel.descripcion,
            direccion: data.hotel.direccion,
            ciudad: data.hotel.ciudad,
            pais: data.hotel.pais,
            estrellas: data.hotel.estrellas, 
            imagen_url: data.hotel.imagen_url,
          },
        });
        // 5. Precargar FormArray: Galería de Imágenes
        // A. Limpiar el FormArray 'galeriaImagenes' primero.
        while (this.galeriaImagenes.length > 0) {
          this.galeriaImagenes.removeAt(0);
        }
        
        // Nuevo código dentro de cargarDatosHotel:
        (data.hotel.imagenes as any[])?.forEach((imgElement: any) => { 
          // 1. Extraer URL: Si es string (URL), usarlo directamente. Si es objeto, usar .url.
          const url = typeof imgElement === 'string' ? imgElement : imgElement?.url;
          // 2. Extraer ALT: Si es objeto, usar .alt. Si no, usar ''
          const alt = typeof imgElement === 'object' ? (imgElement.alt ?? '') : '';
          // 3. Crear FormGroup solo si tenemos una URL
          if(url){
            this.galeriaImagenes.push(this.fb.group({
              url: [(url?? ''), Validators.required], 
              alt: [alt]
            }));
          }
        });
        // A. Limpiar el FormArray 'habitaciones' que inicia con un control vacío por defecto.
        while (this.habitaciones.length > 0) {
          this.habitaciones.removeAt(0);
        }
      
        // B. Iterar sobre los datos de la API y añadir al FormArray
        data.habitaciones.forEach((hab) => {
          // Crea un nuevo FormGroup usando la estructura base (crearHabitacion)
          const habitacionGroup = this.crearHabitacion(); 
          habitacionGroup.patchValue({
            id: hab.id,
            nombre: hab.nombre,
            capacidad_adultos: hab.capacidad_adultos,
            capacidad_ninos: hab.capacidad_ninos,
            precio_por_noche: hab.precio_por_noche,
            cantidad: hab.cantidad,
            descripcion: hab.descripcion,
            // NOTA: Agregar el ID de la habitación para actualizarla en el FormGroup de la habitación
          });
          this.habitaciones.push(habitacionGroup);
          // Inicializar contador con la longitud actual de la descripción
          this.habitacionesDescripcionLengths.push((hab.descripcion || '').toString().length);
        });
        if (this.habitaciones.length === 0) {
          this.agregarHabitacion(); 
        }
        this.cargandoDatosHotel=false;
      },
      error: (error) => {
        this.mensajeError = '❌ Error al cargar los datos del hotel: ' + (error.error?.message || 'Desconocido');
        console.error('Error de carga:', error);
      }
    });
  }
  // ======================================================
  // 📤 Envío del formulario
  // ======================================================
  onSubmit(): void {
    this.enviado = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    // 1. Validar campos de formulario
    if (this.hotelForm.invalid) {
      this.mensajeError = '❌ Por favor, completa todos los campos requeridos correctamente.';
      // CRÍTICO: Marca todos los campos como 'touched' para que Angular muestre los errores visualmente.
      this.hotelForm.markAllAsTouched();
      return;
    }

    // Verificar que envio al backend
    console.log('✅ Estado general:', this.hotelForm.valid);
    console.log('🧱 Formulario completo:', this.hotelForm.value);

    // 2. Validar Rol (Autorización en el frontend)
    const userRole = this.authService.getRole(); 
    if (userRole !== 'proveedor') {
        this.mensajeError = '🔒 Acceso denegado: Solo los proveedores pueden crear hoteles.';
        setTimeout(() => this.router.navigate(['/auth/login']), 1500); 
        return;
    }

    this.enviando = true;
    
    // 3. Obtener y normalizar el payload completo para asegurar que `descripcion` llegue correctamente.
    const raw = this.hotelForm.getRawValue();
    const habitacionesPayload = (raw.habitaciones || []).map((h: any) => ({
      id: h.id ?? null,
      nombre: h.nombre,
      capacidad_adultos: h.capacidad_adultos,
      capacidad_ninos: h.capacidad_ninos,
      precio_por_noche: h.precio_por_noche,
      cantidad: h.cantidad,
      descripcion: (h.descripcion || '').toString().trim(),
    })) as HabitacionUpdatePayload[];

    const payload = {
      hotel: raw.hotel,
      habitaciones: habitacionesPayload
    } as { hotel: HotelCreatePayload, habitaciones: HabitacionUpdatePayload[] };


    // 4. Llamada al servicio, que maneja el encadenamiento de POST /api/hoteles
    // seguido de POST /api/habitaciones/batch
    this.hotelService.updateHotelWithHabitaciones(this.hotelId!,payload).subscribe({
      next: (res) => {
        this.enviando = false;
        this.mensajeExito = '✅ Hotel actualizado correctamente. Redirigiendo a tu panel...';
        
        // Limpiar y resetear el formulario
        this.hotelForm.reset({
            hotel: { estrellas: 3}, // Incluir valores por defecto
            habitaciones: []
        });
        // Asegurar que el FormArray de habitaciones se reinicie con 1 control
        while (this.habitaciones.length > 0) {
            this.habitaciones.removeAt(0);
        }
        this.agregarHabitacion(); 
        
        setTimeout(() => this.router.navigate(['/proveedor']), 1500); 
      },
      error: (error: any) => {
        this.enviando = false;
        // 409: p.ej. eliminar un tipo de habitación con reservas activas o bajar unidades por debajo de lo reservado
        const campos = erroresPorCampo(error);
        const primero = Object.keys(campos)[0];
        this.mensajeError = primero ? `❌ ${campos[primero]}` : `❌ ${mensajeDeError(error, 'No se pudo actualizar el hotel.')}`;
        setTimeout(() => document.getElementById('mensajeError')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
      },
    });
  }
}
