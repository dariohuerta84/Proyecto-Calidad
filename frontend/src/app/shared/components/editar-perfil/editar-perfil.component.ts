import { Component, OnInit, signal, computed, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { finalize, map } from 'rxjs/operators';
import { environment } from '../../../../environments/environment';
import { AuthService, User } from '../../../core/services/auth.service';
import { mensajeDeError, erroresPorCampo } from '../../../core/utils/api-error';

const API = environment.apiUrl;

/** Las dos contraseñas nuevas deben coincidir. */
function coincidenValidator(): ValidatorFn {
  return (g: AbstractControl): ValidationErrors | null => {
    const a = g.get('password')?.value, b = g.get('password_confirmation')?.value;
    return a && b && a !== b ? { passwordMismatch: true } : null;
  };
}

/**
 * HU-V03 — Mi cuenta.
 * - Datos del perfil: PATCH /usuarios/me solo con campos permitidos por rol (nunca rol ni contraseña).
 * - Contraseña: PUT /usuarios/me/password con la contraseña actual.
 * - Imagen de perfil: avatar_url (URL o búsqueda en Unsplash).
 * - Eliminar cuenta: DELETE /usuarios/me; 409 si hay reservas activas.
 */
@Component({
  selector: 'app-editar-perfil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './editar-perfil.component.html',
  styleUrls: ['./editar-perfil.component.css'],
})
export class EditarPerfilComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  perfil = signal<User | null>(null);
  rol = computed(() => this.perfil()?.rol || 'viajero');
  cargando = signal(true);
  errorCarga = signal('');

  form!: FormGroup;
  guardando = signal(false);
  mensaje = signal('');
  tipoMensaje = signal<'success' | 'error'>('success');

  passForm: FormGroup = this.fb.group({
    password_actual: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
    password_confirmation: ['', Validators.required],
  }, { validators: coincidenValidator() });
  cambiandoPass = signal(false);
  mensajePass = signal('');
  tipoMensajePass = signal<'success' | 'error'>('success');
  erroresPass = signal<Record<string, string>>({});
  mostrarPass = signal(false);

  // Imagen
  imageSearchResults: any[] = [];
  buscandoImagenes = signal(false);
  errorImagenes = signal<string | null>(null);

  // Eliminar cuenta
  confirmarEliminar = signal(false);
  eliminando = signal(false);
  errorEliminar = signal('');

  ngOnInit(): void { this.cargar(); }

  cargar(): void {
    this.cargando.set(true);
    this.errorCarga.set('');
    this.http.get<{ data: User }>(`${API}/usuarios/me`).pipe(map(r => r.data), finalize(() => this.cargando.set(false))).subscribe({
      next: u => { this.perfil.set(u); this.construir(u); },
      error: e => this.errorCarga.set(mensajeDeError(e, 'No pudimos cargar tu perfil.')),
    });
  }

  private construir(u: any): void {
    const esProv = u.rol === 'proveedor';
    this.form = this.fb.group({
      email: [{ value: u.email, disabled: true }],
      ruc: [{ value: u.ruc ?? '', disabled: true }],
      nombre: [u.nombre ?? '', esProv ? [] : [Validators.required, Validators.maxLength(100), Validators.pattern(/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$/)]],
      apellido: [u.apellido ?? '', esProv ? [] : [Validators.required, Validators.maxLength(100), Validators.pattern(/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$/)]],
      empresa_nombre: [u.empresa_nombre ?? '', esProv ? [Validators.required, Validators.minLength(2), Validators.maxLength(150)] : []],
      telefono: [u.telefono ?? '', esProv ? [Validators.required, Validators.pattern(/^(\+51\s?)?9\d{8}$/)] : []],
      avatar_url: [u.avatar_url ?? '', [Validators.pattern(/^https?:\/\/.+/i), Validators.maxLength(500)]],
    });
  }

  campoInvalido(c: string): boolean {
    const x = this.form?.get(c);
    return !!x && x.invalid && (x.dirty || x.touched);
  }

  guardarCambios(): void {
    this.mensaje.set('');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const permitidos = this.rol() === 'proveedor' ? ['empresa_nombre', 'telefono', 'avatar_url'] : ['nombre', 'apellido', 'avatar_url'];
    const payload: Record<string, any> = {};
    for (const k of permitidos) {
      if (!this.form.get(k)?.dirty) continue;
      const val = typeof v[k] === 'string' ? v[k].trim() : v[k];
      payload[k] = k === 'avatar_url' && !val ? null : val;
    }
    if (!Object.keys(payload).length) { this.tipoMensaje.set('success'); this.mensaje.set('No hay cambios por guardar.'); return; }

    this.guardando.set(true);
    this.http.patch<{ message: string; data: User }>(`${API}/usuarios/me`, payload).pipe(finalize(() => this.guardando.set(false))).subscribe({
      next: r => {
        this.perfil.set(r.data);
        this.auth.updateUserInState(r.data);
        this.construir(r.data);
        this.tipoMensaje.set('success');
        this.mensaje.set(r.message || 'Perfil actualizado correctamente.');
      },
      error: e => {
        const campos = erroresPorCampo(e);
        Object.entries(campos).forEach(([k, m]) => this.form.get(k)?.setErrors({ servidor: m }));
        this.tipoMensaje.set('error');
        this.mensaje.set(Object.values(campos)[0] || mensajeDeError(e, 'No se pudo actualizar el perfil.'));
      },
    });
  }

  cambiarPassword(): void {
    this.mensajePass.set('');
    this.erroresPass.set({});
    if (this.passForm.invalid) { this.passForm.markAllAsTouched(); return; }
    this.cambiandoPass.set(true);
    this.http.put<{ message: string }>(`${API}/usuarios/me/password`, this.passForm.value).pipe(finalize(() => this.cambiandoPass.set(false))).subscribe({
      next: r => { this.tipoMensajePass.set('success'); this.mensajePass.set(r.message || 'Contraseña actualizada.'); this.passForm.reset(); },
      error: e => {
        this.erroresPass.set(erroresPorCampo(e));
        this.tipoMensajePass.set('error');
        this.mensajePass.set(mensajeDeError(e, 'No se pudo cambiar la contraseña.'));
      },
    });
  }

  eliminarCuenta(): void {
    this.eliminando.set(true);
    this.errorEliminar.set('');
    this.http.delete(`${API}/usuarios/me`).pipe(finalize(() => this.eliminando.set(false))).subscribe({
      next: () => {
        this.confirmarEliminar.set(false);
        this.auth.cleanSession();
        this.router.navigate(['/auth/login']);
      },
      error: e => this.errorEliminar.set(mensajeDeError(e, 'No se pudo eliminar la cuenta.')),
    });
  }

  buscarImagen(q: string): void {
    const t = q.trim();
    if (!t) { this.imageSearchResults = []; return; }
    this.buscandoImagenes.set(true);
    this.errorImagenes.set(null);
    this.http.get<{ images: any[] }>(`${API}/images/search`, { params: { q: t } }).pipe(finalize(() => this.buscandoImagenes.set(false))).subscribe({
      next: r => this.imageSearchResults = r.images ?? [],
      error: () => { this.imageSearchResults = []; this.errorImagenes.set('No se pudieron cargar las imágenes. Puedes pegar una URL directamente.'); },
    });
  }

  elegirImagen(url: string): void {
    const c = this.form.get('avatar_url');
    c?.setValue(url);
    c?.markAsDirty();
    this.imageSearchResults = [];
  }
}
