# API Trouvels360 — Referencia (Etapa 2)

Base: `http://localhost:8000/api` · Formato: JSON · Mensajes en español.

## Autenticación y CSRF

1. `GET /auth/csrf` → cookie `XSRF-TOKEN` (y `{"csrf_token": "..."}`).
2. Toda petición POST/PUT/PATCH/DELETE envía el header `X-XSRF-TOKEN` con el valor de esa cookie. Sin él: **403**.
3. `POST /auth/login` → cookie HttpOnly `access_token` (JWT, 60 min). También se acepta `Authorization: Bearer <jwt>`.
4. Las reservas exigen además el header **`Idempotency-Key`** (8–100 caracteres, p. ej. un UUID).

## Códigos de respuesta

| Código | Significado |
| --- | --- |
| 200 / 201 / 204 | OK / creado / eliminado |
| 400 | Falta `Idempotency-Key` |
| 401 | Sin sesión o token inválido/expirado |
| 403 | Sin CSRF, rol incorrecto o recurso de otro dueño |
| 404 | No existe o no es tuyo (reservas ajenas devuelven 404) |
| 409 | Conflicto de negocio: sin unidades/cupos, estado incompatible, reservas activas, duplicado |
| 422 | Validación: `{"message", "errors": {"campo": ["..."]}}` |
| 429 | Demasiados intentos de login |
| 503 | Microservicio de itinerarios no disponible |

## Endpoints

### Cuentas (TRV-101 a TRV-105)
| Método | Ruta | Auth | Notas |
| --- | --- | --- | --- |
| POST | `/auth/register` | — | `rol` viajero: nombre, apellido · proveedor: empresa_nombre, ruc (11), telefono (se normaliza a `+51 9XXXXXXXX`). Siempre `password` + `password_confirmation`. 201 |
| POST | `/auth/login` | — | `email`, `password`. 401 credenciales, 429 tras 6 intentos/min |
| GET | `/auth/me` | ✔ | Perfil |
| POST | `/auth/logout` | ✔ | Invalida el token en el servidor |
| PATCH | `/usuarios/me` | ✔ | Viajero: nombre, apellido, avatar_url · Proveedor: empresa_nombre, telefono, avatar_url. `rol` → 422 |
| PUT | `/usuarios/me/password` | ✔ | `password_actual`, `password`, `password_confirmation` |
| DELETE | `/usuarios/me` | ✔ | 409 con reservas activas |

### Hoteles y aprobación (TRV-201 a TRV-204)
| Método | Ruta | Auth | Notas |
| --- | --- | --- | --- |
| GET | `/hoteles?ciudad&check_in&check_out&adultos&ninos&habitaciones&sort&page&per_page` | — | Paginado (12). Con fechas: solo hoteles con unidades libres en todo el rango |
| GET | `/hoteles/{id}` | — | Detalle, habitaciones, calificación y últimas 10 reseñas |
| GET | `/hoteles/{id}/disponibilidad?check_in&check_out&adultos&ninos&habitaciones` | — | Habitaciones con `unidades_disponibles` |
| POST | `/reservas-habitaciones/cotizar` | — | `{fecha_inicio, fecha_fin, items:[{habitacion_id, cantidad}]}` → subtotal, impuesto (18%), total |
| POST | `/reservas-habitaciones` | ✔ viajero | Igual que cotizar + `pago{titular, numero(16, Luhn), vencimiento MM/AA, cvc}` + `Idempotency-Key`. 201 PENDIENTE · 409 sin unidades (no registra ningún tipo) |
| POST | `/proveedor/reservas-habitaciones/{id}/aceptar` | ✔ proveedor dueño | pendiente → confirmada. 409 si no está pendiente, 403 si es de otro |
| POST | `/proveedor/reservas-habitaciones/{id}/rechazar` | ✔ proveedor dueño | `{motivo}` 10–300. pendiente → rechazada (libera unidades) |

### Tours (TRV-301 a TRV-303)
| Método | Ruta | Auth | Notas |
| --- | --- | --- | --- |
| GET | `/tours?destino&categoria&checkIn&checkOut&cupos&sort&page&per_page` | — | Solo tours con salidas futuras con cupo. Categorías: Aventura, Gastronomía, Cultura, Relajación |
| GET | `/tours/{id}` | — | Detalle con salidas futuras programadas, actividades, items y reseñas |
| POST | `/tours/salidas/{salida}/cotizar` | — | `{personas}` → subtotal, impuesto, total |
| POST | `/tours/salidas/{salida}/reservas` | ✔ viajero | `{personas, pago}` + `Idempotency-Key`. 201 CONFIRMADA · 409 cerrada/pasada/sin cupo |

### Postventa (TRV-401 a TRV-404)
| Método | Ruta | Auth | Notas |
| --- | --- | --- | --- |
| GET | `/mis-reservas?estado&page&per_page` | ✔ | Hotel, paginado (10). Estados: pendiente, confirmada, rechazada, cancelada |
| GET | `/mis-reservas/{id}` | ✔ | 404 si no es tuya |
| GET | `/tours/mis-reservas?estado&page` | ✔ viajero | Tour, paginado, con nombre del tour y aviso si la salida fue cancelada |
| POST | `/reservas-habitaciones/{id}/cancelar` | ✔ | 409 ya cancelada, rechazada o estadía iniciada · 404 ajena |
| POST | `/tours/reservas/{id}/cancelar` | ✔ | Devuelve las personas al cupo |
| GET | `/reviews?servicio_id&page&per_page` | — | Paginado (10) |
| GET | `/reviews/resumen?servicio_id` | — | promedio, total y distribución del conjunto completo |
| GET | `/reviews/puedo-opinar?servicio_id` | ✔ | `{puede, motivo, mi_resena}` |
| POST | `/reviews` | ✔ | `{servicio_id, calificacion 1–5, comentario 10–300}`. 403 sin reserva finalizada · 409 duplicada |
| PUT/DELETE | `/reviews/{id}` | ✔ autor | 403 si no eres el autor |
| POST | `/itineraries` | ✔ | `{destino, hotel_id, fecha_checkin, fecha_checkout (≤14 días), interes}` → microservicio Python. 503 si no responde en 10 s |

### Portal del proveedor (TRV-501 a TRV-508)
| Método | Ruta | Notas |
| --- | --- | --- |
| GET | `/proveedor/servicios?tipo&search&page&per_page` | Paginado (15) con reservas por estado y reseñas |
| GET | `/proveedor/indicadores` | Pendientes por decidir, reservas por estado, ingresos confirmados, reseñas |
| GET | `/proveedor/reservas?tipo&estado&servicio_id&page` | Bandeja: pendientes primero, con cliente |
| GET | `/proveedor/reservas/hotel/{id}` | 404 si no es de tus hoteles |
| POST / PUT / DELETE | `/hoteles`, `/hoteles/{id}` | Crear exige ≥1 habitación e imagen; galería ≤5 |
| POST / PUT / DELETE | `/hoteles/{id}/habitaciones`, `/habitaciones/{id}` | 409 si bajas unidades bajo lo reservado o eliminas con reservas activas |
| POST / PUT / DELETE | `/tours`, `/tours/{id}` | Editar no toca salidas |
| GET / POST / PUT / DELETE | `/tours/{id}/salidas[/{s}]` | Duplicada 409, fecha pasada 422, cupo < reservado 422, eliminar con confirmadas 409 |
| GET / POST / PUT / DELETE | `/tours/{id}/actividades[/{a}]` | Sin `orden` va al final; insertar desplaza |
| DELETE | `/servicios/{id}` | Solo el dueño (403), 409 con reservas activas, borrado lógico |
