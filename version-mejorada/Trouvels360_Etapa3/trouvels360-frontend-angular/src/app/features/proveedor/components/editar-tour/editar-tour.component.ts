import { RouterModule } from '@angular/router';
import { Component, OnInit, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  ReactiveFormsModule,
  FormControl,
} from '@angular/forms';
import { Router } from '@angular/router';
import { TourService, TourData, TourDetalleApiResponse, TourDetalles} from '../../../tours/services/tour.service'; 
import { AuthService } from '../../../../core/services/auth.service';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

/**
 * Componente dedicado a la creación/edición de Tours.
 */
interface SalidaData {
  fecha: string;
  hora: string;
  cupo_total: number;
  cupo_reservado: number;
  estado: string;
}

import { mensajeDeError, erroresPorCampo } from '../../../../core/utils/api-error';

@Component({
  selector: 'app-editar-tour',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './editar-tour.component.html',
  styleUrls: ['./editar-tour.component.css'],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class EditarTourComponent implements OnInit {
  tourId: number | null = null;
  cargandoDatos: boolean = true;
  tourForm!: FormGroup;
  nuevoItemForm!: FormGroup;
  // 🆕 Propiedad para almacenar el ID del tour a editar
  enviado = false;
  enviando = false;
  mensajeExito = '';
  mensajeError = '';
  // 🆕 PROPIEDAD PARA ALMACENAR LA FECHA MÍNIMA (HOY)
  minDate: string; 

  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private tourService = inject(TourService);
  private authService = inject(AuthService);
  private router = inject(Router);

  constructor() {
    // 🆕 Calculamos la fecha actual en formato YYYY-MM-DD al inicializar el componente.
    const today = new Date();
    // Usamos toISOString().split('T')[0] para asegurar el formato YYYY-MM-DD
    this.minDate = today.toISOString().split('T')[0]; 

    this.nuevoItemForm = this.fb.group({
      nombre: ['', Validators.required],
      icono: [''],
    });
  }

  ngOnInit(): void {
    this.crearFormulario();
    this.route.paramMap.subscribe({
      next: (params: ParamMap) => {
        const idString = params.get('id'); // 'id' debe coincidir con el nombre de tu ruta: 'editar-tour/:id'
        
        if (idString) {
          // El '+' convierte el string del parámetro a número (number)
          this.tourId = +idString; 
          
          // 3. Llamar a la función de carga
          this.cargarDatosTour(this.tourId);
          
        } else {
          console.error('No se encontró el ID del tour en la ruta.');
          // Manejar la falta de ID (ej. redireccionar o mostrar error)
        }
      },
      error: (err) => {
        console.error('Error al leer los parámetros de la ruta:', err);
      }
    });
  }

  // ================================================
  // 🏗️ Construcción del formulario
  // ================================================
  crearFormulario(): void {
    this.tourForm = this.fb.group({
      tour: this.fb.group({
        nombre: ['', [Validators.required]],
        descripcion: ['', [Validators.required, Validators.minLength(10)]],
        //direccion: ['', [Validators.required]], No hay dirección en tours
        ciudad: ['', [Validators.required]], // ✅ Campo 1 para ubicación
        pais: ['', [Validators.required]],   // ✅ Campo 2 para ubicación
        precio: [0, [Validators.required, Validators.min(0)]],
        categoria: ['', [Validators.required]],
        duracion: [1, [Validators.required, Validators.min(1)]],
        imagen_url: ['', [Validators.required]],
        // Galería de imágenes dinámica
        imagenes: this.fb.array<FormGroup>([]),
        // Items dinámicos (cosas para llevar)
        items: this.fb.array<FormGroup>([]),
      }),
      // FormArray para salidas dinámicas
      salidas: this.fb.array([]),
    });
  }

  // ================================================
  // 🧾 Getters para acceder fácilmente a los campos
  // ================================================
  get tourGroup(): FormGroup {
    return this.tourForm.get('tour') as FormGroup;
  }

  get galeriaImagenes(): FormArray<FormGroup> {
    return this.tourGroup.get('imagenes') as FormArray<FormGroup>;
  }

  get items(): FormArray<FormGroup> {
    return this.tourGroup.get('items') as FormArray<FormGroup>;
  }

  get salidas(): FormArray {
    return this.tourForm.get('salidas') as FormArray;
  }

  // ================================================
  // 🖼️ Galería de imágenes
  // ================================================
  agregarImagen(url: string, alt:string=''): void {
     const MAX_IMAGES = 5; // Definimos el límite máximo de imágenes
  
    // 1. Verificación de límite máximo
    if (this.galeriaImagenes.length >= MAX_IMAGES) {
      console.warn(`[GALERIA] Límite de ${MAX_IMAGES} imágenes alcanzado. No se agregará la URL.`);
      return;
    }
    
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
    this.galeriaImagenes.removeAt(index);
  }

  // ================================================
  // ➕/❌ Items (cosas para llevar)
  // ================================================
  // ✅ Lista de íconos Font Awesome a mostrar
  iconosFontAwesome: string[] = [
    'fa-solid fa-suitcase',          // Maleta
    'fa-solid fa-glasses',           // Gafas de sol
    'fa-solid fa-passport',          // Pasaporte
    'fa-solid fa-camera',            // Cámara
    'fa-solid fa-charging-station',  // Cargador / batería
    'fa-solid fa-headphones',        // Audífonos
    'fa-solid fa-tshirt',            // Ropa
    'fa-solid fa-umbrella-beach',    // Sombrilla / playa
    'fa-solid fa-bottle-droplet',    // Botella de agua
    'fa-solid fa-sun',               // Protector solar
    'fa-solid fa-shoe-prints',       // Zapatillas
    'fa-solid fa-map',               // Mapa
    'fa-solid fa-plane',             // Avión
    'fa-solid fa-ticket',            // Ticket / boleto
    'fa-solid fa-bed',               // Dormir / descanso
    'fa-solid fa-hat-cowboy',        // Sombrero
    'fa-solid fa-person-hiking',     // Excursionismo
    'fa-solid fa-compass',           // Brújula
    'fa-solid fa-binoculars',        // Binoculares
    'fa-solid fa-mountain',          // Montaña / aventura
    'fa-solid fa-swimmer',           // Ropa de baño
    'fa-solid fa-first-aid',         // Botiquín
    'fa-solid fa-book',              // Libro
    'fa-solid fa-wallet',            // Billetera
    'fa-solid fa-mobile-screen',     // Celular
    'fa-solid fa-laptop',            // Laptop
    'fa-solid fa-bottle-water',      // Hidratación
    'fa-solid fa-cookie-bite',       // Snacks
    'fa-solid fa-cloud-sun',         // Clima / abrigo
    'fa-solid fa-car',            // Cepillo (aseo)
  ];
  // Manejo del desplegable para elegir icono
  // 🔁 Reutilizamos los toggles
  mostrarEmojiPicker: boolean[] = [];  
  mostrarEmojiNuevo = false;

  toggleEmojiPicker(index: number): void {
    this.mostrarEmojiPicker[index] = !this.mostrarEmojiPicker[index];
  }
  toggleEmojiNuevo(): void {
    this.mostrarEmojiNuevo = !this.mostrarEmojiNuevo;
  }
  // ✅ Seleccionar ícono existente
  seleccionarIcono(icon: string, index: number): void {
    const items = this.tourForm.get('tour.items') as FormArray;
    const item = items.at(index);
    item.get('icono')?.setValue(icon);
    this.mostrarEmojiPicker[index] = false;
  }

  // ✅ Seleccionar ícono nuevo
  seleccionarIconoNuevo(icon: string): void {
    this.nuevoItemForm.get('icono')?.setValue(icon);
    this.mostrarEmojiNuevo = false;
  }

  agregarItem(): void {
    const MAX_ITEMS = 5; // ✅ Límite máximo de items

    if (this.nuevoItemForm.invalid) {
      return; // No hacer nada si el formulario del nuevo ítem es inválido
    }

    // ✅ Verificar límite máximo
    if (this.items.length >= MAX_ITEMS) {
      console.warn(`[ITEMS] Límite de ${MAX_ITEMS} items alcanzado. No se agregará más.`);
      return;
    }

    const { nombre, icono } = this.nuevoItemForm.value;

    // Creamos un nuevo FormGroup para el FormArray, asegurándonos de que tenga sus propios validadores.
    const itemGroup = this.fb.group({
      nombre: [nombre, [Validators.required]],
      icono: [icono]
    });

    this.items.push(itemGroup);
    this.nuevoItemForm.reset({ nombre: '', icono: '' }); // Limpiar el formulario del nuevo ítem
  }
  
  quitarItem(index: number): void {
    this.items.removeAt(index);
  }

  // ================================================
  // 🗓️ Gestión de salidas dinámicas
  // ================================================
  agregarSalida(): void {
    const salidaForm = this.fb.group({
      fecha: ['', [Validators.required]],
      hora: ['', [Validators.required]],
      cupo_total: [1, [Validators.required, Validators.min(1)]],
      cupo_reservado: [0, [Validators.required, Validators.min(0)]],
      estado: ['programada', [Validators.required]],
    });
    this.salidas.push(salidaForm);
  }

  eliminarSalida(index: number): void {
    this.salidas.removeAt(index);
  }
  // ================================================
  // Cargar datos 
  // ================================================
  cargarDatosTour(id: number): void {
    this.cargandoDatos = true;
    // 2. Llamar al servicio para obtener los datos
    this.tourService.getTourById(id).subscribe({
      next: (data: TourDetalles) => {

        // 3. Precargar el formulario principal (Tour Group)
        this.tourForm.patchValue({
          tour: {
            nombre: data.nombre,
            descripcion: data.descripcion,
            ciudad: data.ciudad,
            pais: data.pais,
            precio: data.tour?.precio,
            categoria: data.tour?.categoria,
            duracion: data.tour?.duracion,
            imagen_url: data.imagen_url,
          }
        });
        const tourItems = data.tour?.items;
        // 4. Precargar FormArray: Items (Cosas para llevar)
        if (tourItems && tourItems.length>0) {
          this.items.clear();

          tourItems.forEach((item) => { 
            this.items.push(this.fb.group({
              nombre: [item.nombre, Validators.required],
              icono: [item.icono || ''], 
            }));
          });
        }

        // 5. Precargar FormArray: Galería de Imágenes
        data.imagenes?.forEach((img) => { 
          this.galeriaImagenes.push(this.fb.group({
            url: [img.url, Validators.required], // Usar 'url'
            alt: [''] // No hay campo 'alt' en tu interfaz, se deja vacío.
          }));
        });

        // Las salidas se gestionan en su propia pantalla (no se precargan aquí).
        this.cargandoDatos = false; // Desactivar la carga
        
      },
      error: (error) => {
        this.mensajeError = '❌ Error al cargar los datos del tour: ' + (error.error?.message || 'Desconocido');
        console.error('Error de carga:', error);
      }
    });
  }
  // ================================================
  // 📤 Envío del formulario
  // ================================================
  onSubmit(): void {
    this.enviado = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    // 1. Verificar si tenemos el ID del tour a editar
    if (!this.tourId){
      this.mensajeError = '❌ Error interno: ID del tour no encontrado para actualizar.';
      console.error('El ID del tour (this.tourId) es null o undefined.');
      return;
    }

    if (this.tourForm.invalid) {
      this.mensajeError = '❌ Por favor, completa todos los campos requeridos correctamente.';
      this.tourForm.markAllAsTouched();
      // Scroll al mensaje
      const el = document.getElementById('mensajeError');
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
      return;
    }


    const userRole = this.authService.getRole();
    if (userRole !== 'proveedor') {
      this.mensajeError = '🔒 Acceso denegado: Solo los proveedores pueden crear tours.';
      setTimeout(() => this.router.navigate(['/auth/login']), 1500);
      return;
    }
    // ==================================================================
    this.enviando = true;
    const tourData = this.tourGroup.getRawValue() as TourData;
    
    // Las salidas NO se envían: se gestionan en /proveedor/tour/:id/programa
    const { salidas: _omitidas, ...soloTour } = tourData as any;
    const payload = {
      ...soloTour,
      imagenes: (tourData.imagenes??[]).map(img => ({
        url: img.url,
        alt: img.alt ?? undefined,
      })),
      items: (tourData.items ?? []).map(item => ({
        nombre: item.nombre,
        icono: item.icono ?? undefined,
      })),
    };


    this.tourService.updateTour(this.tourId, payload).subscribe({
      next: () => {
        this.enviando = false;
        this.mensajeExito = `✅ Tour actualizado con éxito. Redirigiendo a tu panel...`;
        setTimeout(() => this.router.navigate(['/proveedor']), 1500);
      },
      error: (error: any) => {
        this.enviando = false;
        const campos = erroresPorCampo(error);
        const primero = Object.keys(campos)[0];
        this.mensajeError = primero ? `❌ ${campos[primero]}` : `❌ ${mensajeDeError(error, 'No se pudo actualizar el tour.')}`;
      },
    });
  }
}
