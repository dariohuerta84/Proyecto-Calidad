# Matriz de trazabilidad

[← Documentación](../README.md) · [Historias](../1-historias-de-usuario/README.md) · [Casos](../2-pruebas/casos-de-prueba.md) · [Defectos](defectos.md)

Recorrido completo: **historia → criterio de aceptación → caso de prueba → herramienta → resultado**. La misma información está en [`matriz-de-trazabilidad.csv`](matriz-de-trazabilidad.csv) (separador `;`, abre en Excel).

## Cobertura

| Rol | Historias | Criterios | Criterios con caso | Casos distintos | Casos que pasaron |
|---|---|---|---|---|---|
| Viajero | 15 | 76 | 76 (100 %) | 104 | 77 |
| Proveedor | 15 | 54 | 54 (100 %) | 67 | 54 |
| **Total** | **30** | **130** | **130 (100 %)** | **160** | **121** |

Algunos casos verifican criterios de las dos vistas del mismo flujo (por ejemplo TC-WEB-013: el proveedor acepta y el viajero ve la reserva confirmada), por eso la suma por rol es mayor que el total.

## Resumen por historia

| Historia | Criterios | API (Karate) | Web (Selenium) | Móvil (Appium) | Rendimiento (JMeter) | Manual | Pasó |
|---|---|---|---|---|---|---|---|
| **HU-V01** Crear mi cuenta de viajero | 5 | TC-API-001, TC-API-002, TC-API-003, TC-API-004 | TC-WEB-001, TC-WEB-002 | TC-MOB-001, TC-MOB-002 | — | — | 6/8 |
| **HU-V02** Iniciar sesión y volver a lo que estaba haciendo | 7 | TC-API-008, TC-API-009, TC-API-010, TC-API-011 | TC-WEB-004, TC-WEB-005, TC-WEB-033 | TC-MOB-004, TC-MOB-005 | TC-PERF-001 | — | 7/10 |
| **HU-V03** Actualizar mis datos y mi contraseña | 6 | TC-API-013, TC-API-014, TC-API-015, TC-API-016 | TC-WEB-006 | TC-MOB-006, TC-MOB-007 | — | TC-MAN-001 | 5/8 |
| **HU-V04** Eliminar mi cuenta | 4 | TC-API-017, TC-API-018, TC-API-019 | TC-WEB-007 | TC-MOB-008 | — | — | 4/5 |
| **HU-V05** Buscar hoteles disponibles | 7 | TC-API-020, TC-API-021, TC-API-022 | TC-WEB-008, TC-WEB-009, TC-WEB-031 | TC-MOB-009, TC-MOB-010 | TC-PERF-002 | — | 6/9 |
| **HU-V06** Ver un hotel y cotizar mi estadía | 5 | TC-API-023, TC-API-024, TC-API-025, TC-API-079 | TC-WEB-010 | TC-MOB-011, TC-MOB-012 | — | — | 5/7 |
| **HU-V07** Reservar un hotel con pago simulado | 6 | TC-API-026, TC-API-027, TC-API-028, TC-API-029, TC-API-030 | TC-WEB-011, TC-WEB-012 | TC-MOB-013, TC-MOB-014 | TC-PERF-003 | — | 8/10 |
| **HU-V08** Buscar tours | 5 | TC-API-036, TC-API-037 | TC-WEB-015, TC-WEB-031, TC-WEB-032 | TC-MOB-017 | TC-PERF-004 | — | 5/7 |
| **HU-V09** Ver un tour y cotizar | 4 | TC-API-038, TC-API-039, TC-API-040, TC-API-080 | TC-WEB-016 | TC-MOB-018 | — | — | 5/6 |
| **HU-V10** Reservar un tour | 5 | TC-API-041, TC-API-042, TC-API-043, TC-API-081 | TC-WEB-017 | TC-MOB-019 | TC-PERF-005 | — | 6/7 |
| **HU-V11** Ver mis reservas | 3 | TC-API-044, TC-API-046 | TC-WEB-018 | TC-MOB-020, TC-MOB-021 | — | — | 3/5 |
| **HU-V12** Saber si el hotel aceptó o rechazó mi solicitud | 4 | TC-API-031, TC-API-041, TC-API-045, TC-API-082 | TC-WEB-013, TC-WEB-014, TC-WEB-019 | — | — | — | 7/7 |
| **HU-V13** Cancelar una reserva | 5 | TC-API-047, TC-API-048, TC-API-049 | TC-WEB-020 | TC-MOB-022 | — | TC-MAN-002 | 4/6 |
| **HU-V14** Leer y escribir reseñas | 5 | TC-API-050, TC-API-051, TC-API-052, TC-API-053 | TC-WEB-021 | TC-MOB-023 | — | — | 5/6 |
| **HU-V15** Recibir un itinerario sugerido | 5 | TC-API-054, TC-API-055 | TC-WEB-022 | TC-MOB-024 | — | TC-MAN-003 | 3/5 |
| **HU-P01** Crear mi cuenta de proveedor | 4 | TC-API-003, TC-API-005, TC-API-006, TC-API-007 | TC-WEB-002, TC-WEB-003 | TC-MOB-002, TC-MOB-003 | — | — | 6/8 |
| **HU-P02** Entrar a mi panel y mantener los datos de mi empresa | 3 | TC-API-008, TC-API-012, TC-API-014 | TC-WEB-004 | — | — | — | 4/4 |
| **HU-P03** Ver mis publicaciones | 3 | TC-API-056, TC-API-058 | TC-WEB-023 | TC-MOB-025 | TC-PERF-006 | — | 3/5 |
| **HU-P04** Ver los indicadores de mi negocio | 3 | TC-API-050, TC-API-057 | TC-WEB-023 | TC-MOB-025 | — | — | 3/4 |
| **HU-P05** Publicar un hotel | 3 | TC-API-059, TC-API-060, TC-API-084 | TC-WEB-024 | TC-MOB-026 | — | — | 4/5 |
| **HU-P06** Editar un hotel | 2 | TC-API-061, TC-API-083 | — | — | — | — | 2/2 |
| **HU-P07** Gestionar los tipos de habitación | 5 | TC-API-062, TC-API-063, TC-API-064, TC-API-085 | TC-WEB-025 | TC-MOB-027 | — | — | 5/6 |
| **HU-P08** Publicar un tour | 2 | TC-API-065, TC-API-067 | TC-WEB-026 | TC-MOB-028 | — | — | 3/4 |
| **HU-P09** Editar un tour | 2 | TC-API-066, TC-API-067 | — | — | — | — | 2/2 |
| **HU-P10** Armar el programa de actividades del tour | 4 | TC-API-068, TC-API-069 | TC-WEB-027 | TC-MOB-029 | — | — | 3/4 |
| **HU-P11** Programar salidas y cupos | 6 | TC-API-070, TC-API-071, TC-API-072 | TC-WEB-028 | TC-MOB-030 | — | — | 4/5 |
| **HU-P12** Ver las reservas que recibo | 5 | TC-API-073, TC-API-074, TC-API-075 | TC-WEB-029, TC-WEB-032 | TC-MOB-031 | — | — | 5/6 |
| **HU-P13** Aceptar una solicitud de hotel | 4 | TC-API-031, TC-API-033, TC-API-034 | TC-WEB-013 | TC-MOB-015 | — | — | 4/5 |
| **HU-P14** Rechazar una solicitud de hotel | 3 | TC-API-032, TC-API-035 | TC-WEB-014 | TC-MOB-016 | — | — | 3/4 |
| **HU-P15** Eliminar una publicación | 5 | TC-API-076, TC-API-077, TC-API-078, TC-API-086 | TC-WEB-030 | TC-MOB-032 | — | — | 5/6 |

## Detalle por criterio

### Viajero

| Historia | Criterio | Casos | Resultado |
|---|---|---|---|
| HU-V01 | CA-V01-01 Registro exitoso | TC-API-001, TC-WEB-001, TC-MOB-001 | Pasó |
| HU-V01 | CA-V01-02 Datos incompletos o inválidos | TC-API-002, TC-WEB-002 | Pasó |
| HU-V01 | CA-V01-03 Contraseñas distintas | TC-API-002, TC-WEB-002, TC-MOB-002 | Pasó |
| HU-V01 | CA-V01-04 Correo ya registrado | TC-API-003 | Pasó |
| HU-V01 | CA-V01-05 Rol no permitido | TC-API-004 | Pasó |
| HU-V02 | CA-V02-01 Ingreso correcto | TC-API-008, TC-WEB-004, TC-MOB-004 | Pasó |
| HU-V02 | CA-V02-02 Vuelvo a mi reserva | TC-WEB-005, TC-MOB-005 | Pasó |
| HU-V02 | CA-V02-03 Datos incorrectos | TC-API-010, TC-MOB-004 | Pasó |
| HU-V02 | CA-V02-04 Sesión vencida | TC-WEB-033, TC-API-011 | Pasó |
| HU-V02 | CA-V02-05 Cerrar sesión de verdad | TC-API-011, TC-MOB-004 | Pasó |
| HU-V02 | CA-V02-06 Protección contra peticiones falsificadas | TC-API-009 | Pasó |
| HU-V02 | CA-V02-07 Login con muchos usuarios a la vez | TC-PERF-001 | Pendiente |
| HU-V03 | CA-V03-01 Guardar mis datos | TC-API-013, TC-WEB-006, TC-MOB-006 | Pasó |
| HU-V03 | CA-V03-02 Datos inválidos | TC-API-014 | Pasó |
| HU-V03 | CA-V03-03 No puedo cambiar mi rol | TC-API-015 | Pasó |
| HU-V03 | CA-V03-04 Cambiar la contraseña | TC-API-016, TC-MOB-007 | Pasó |
| HU-V03 | CA-V03-05 Contraseña actual incorrecta | TC-API-016 | Pasó |
| HU-V03 | CA-V03-06 Error del servidor | TC-MAN-001 | Pendiente |
| HU-V04 | CA-V04-01 Me arrepiento | TC-WEB-007 | Pasó |
| HU-V04 | CA-V04-02 Eliminación permitida | TC-API-018, TC-MOB-008 | Pasó |
| HU-V04 | CA-V04-03 Bloqueo por reservas activas | TC-API-017 | Pasó |
| HU-V04 | CA-V04-04 El historial se conserva | TC-API-019 | Pasó |
| HU-V05 | CA-V05-01 Búsqueda válida | TC-API-020, TC-WEB-008, TC-MOB-009 | Pasó |
| HU-V05 | CA-V05-02 Las solicitudes pendientes ocupan | TC-API-022 | Pasó |
| HU-V05 | CA-V05-03 Fechas inválidas | TC-API-021, TC-WEB-009 | Pasó |
| HU-V05 | CA-V05-04 Cantidades fuera de rango | TC-API-021 | Pasó |
| HU-V05 | CA-V05-05 Sin resultados no es lo mismo que un error | TC-MOB-010 | Pendiente |
| HU-V05 | CA-V05-06 Conservo mi búsqueda | TC-WEB-031 | Pasó |
| HU-V05 | CA-V05-07 Catálogo con mucho tráfico | TC-PERF-002 | Pendiente |
| HU-V06 | CA-V06-01 Detalle completo | TC-API-023 | Pasó |
| HU-V06 | CA-V06-02 Elegir varios tipos | TC-WEB-010, TC-MOB-011 | Pasó |
| HU-V06 | CA-V06-03 Cálculo con IGV | TC-API-024, TC-WEB-010, TC-MOB-011 | Pasó |
| HU-V06 | CA-V06-04 No puedo pedir más de lo disponible | TC-API-025, TC-MOB-012 | Pasó |
| HU-V06 | CA-V06-05 Cotizar no reserva | TC-API-079 | Pasó |
| HU-V07 | CA-V07-01 Solicitud registrada | TC-API-026, TC-WEB-011, TC-MOB-013 | Pasó |
| HU-V07 | CA-V07-02 Pago inválido | TC-API-027, TC-WEB-012, TC-MOB-014 | Pasó |
| HU-V07 | CA-V07-03 Mis habitaciones quedan apartadas | TC-API-028 | Pasó |
| HU-V07 | CA-V07-04 Se acabaron mientras pagaba | TC-API-029 | Pasó |
| HU-V07 | CA-V07-05 Nunca se vende de más | TC-PERF-003 | Pasó |
| HU-V07 | CA-V07-06 Sin duplicados al reintentar | TC-API-030 | Pasó |
| HU-V08 | CA-V08-01 Búsqueda válida | TC-API-036, TC-WEB-015, TC-MOB-017 | Pasó |
| HU-V08 | CA-V08-02 Filtros inválidos | TC-API-037 | Pasó |
| HU-V08 | CA-V08-03 Sin resultados no es lo mismo que un error | TC-WEB-032 | Pasó |
| HU-V08 | CA-V08-04 Conservo mi búsqueda | TC-WEB-031 | Pasó |
| HU-V08 | CA-V08-05 Búsqueda con mucho tráfico | TC-PERF-004 | Pendiente |
| HU-V09 | CA-V09-01 Detalle | TC-API-038 | Pasó |
| HU-V09 | CA-V09-02 Cálculo con IGV | TC-API-039, TC-WEB-016, TC-MOB-018 | Pasó |
| HU-V09 | CA-V09-03 Selección no válida | TC-API-040, TC-MOB-018 | Pasó |
| HU-V09 | CA-V09-04 Tour sin salidas | TC-API-080 | Pasó |
| HU-V10 | CA-V10-01 Reserva confirmada | TC-API-041, TC-WEB-017, TC-MOB-019 | Pasó |
| HU-V10 | CA-V10-02 Pago inválido o desisto | TC-API-081 | Pasó |
| HU-V10 | CA-V10-03 Salida no disponible | TC-API-042 | Pasó |
| HU-V10 | CA-V10-04 Nunca se venden cupos de más | TC-PERF-005 | Pasó |
| HU-V10 | CA-V10-05 Sin duplicados al reintentar | TC-API-043 | Pasó |
| HU-V11 | CA-V11-01 Solo las mías, con todos sus datos | TC-API-044, TC-WEB-018 | Pasó |
| HU-V11 | CA-V11-02 Lo mismo en la web y en la app | TC-API-044, TC-WEB-018, TC-MOB-020, TC-MOB-021 | Pasó |
| HU-V11 | CA-V11-03 Vacío, error y reservas ajenas | TC-API-046 | Pasó |
| HU-V12 | CA-V12-01 Pendiente o confirmada | TC-API-045, TC-API-031, TC-WEB-013 | Pasó |
| HU-V12 | CA-V12-02 Rechazada con motivo | TC-API-045, TC-WEB-019, TC-WEB-014 | Pasó |
| HU-V12 | CA-V12-03 Los tours no esperan aprobación | TC-API-041 | Pasó |
| HU-V12 | CA-V12-04 Salida cancelada por el proveedor | TC-API-082 | Pasó |
| HU-V13 | CA-V13-01 Me arrepiento | TC-WEB-020 | Pasó |
| HU-V13 | CA-V13-02 Cancelación exitosa | TC-API-047, TC-WEB-020, TC-MOB-022 | Pasó |
| HU-V13 | CA-V13-03 El tour devuelve los cupos exactos | TC-API-048 | Pasó |
| HU-V13 | CA-V13-04 Reserva no cancelable | TC-API-049 | Pasó |
| HU-V13 | CA-V13-05 Se cortó la conexión | TC-MAN-002, TC-API-049 | Pasó |
| HU-V14 | CA-V14-01 Leer opiniones | TC-API-050 | Pasó |
| HU-V14 | CA-V14-02 Publicar mi reseña | TC-API-051, TC-WEB-021, TC-MOB-023 | Pasó |
| HU-V14 | CA-V14-03 Reseña no permitida | TC-API-051, TC-API-052 | Pasó |
| HU-V14 | CA-V14-04 Editar o borrar solo la mía | TC-API-053, TC-WEB-021 | Pasó |
| HU-V14 | CA-V14-05 Una reseña por servicio | TC-API-053 | Pasó |
| HU-V15 | CA-V15-01 Propuesta generada | TC-API-054, TC-WEB-022, TC-MOB-024 | Pasó |
| HU-V15 | CA-V15-02 Es una sugerencia, no una reserva | TC-WEB-022 | Pasó |
| HU-V15 | CA-V15-03 Sigo con mi reserva | TC-MOB-024 | Pendiente |
| HU-V15 | CA-V15-04 Datos inválidos | TC-API-055 | Pasó |
| HU-V15 | CA-V15-05 Servicio de itinerarios caído | TC-MAN-003 | Pendiente |

### Proveedor

| Historia | Criterio | Casos | Resultado |
|---|---|---|---|
| HU-P01 | CA-P01-01 Registro exitoso | TC-API-005, TC-WEB-003, TC-MOB-003 | Pasó |
| HU-P01 | CA-P01-02 RUC inválido o repetido | TC-API-006 | Pasó |
| HU-P01 | CA-P01-03 Celular inválido | TC-API-007 | Pasó |
| HU-P01 | CA-P01-04 Correo repetido o contraseñas distintas | TC-API-003, TC-WEB-002, TC-MOB-002 | Pasó |
| HU-P02 | CA-P02-01 Llego a mi panel | TC-API-008, TC-WEB-004 | Pasó |
| HU-P02 | CA-P02-02 El panel es solo para proveedores | TC-API-012, TC-WEB-004 | Pasó |
| HU-P02 | CA-P02-03 Celular de la empresa inválido | TC-API-014 | Pasó |
| HU-P03 | CA-P03-01 Solo las mías, por páginas | TC-API-056, TC-WEB-023, TC-MOB-025 | Pasó |
| HU-P03 | CA-P03-02 Panel vacío o acceso no autorizado | TC-API-058 | Pasó |
| HU-P03 | CA-P03-03 Panel con muchos proveedores a la vez | TC-PERF-006 | Pendiente |
| HU-P04 | CA-P04-01 Cada estado por separado | TC-API-057, TC-WEB-023, TC-MOB-025 | Pasó |
| HU-P04 | CA-P04-02 Reseñas sobre el total | TC-API-050 | Pasó |
| HU-P04 | CA-P04-03 Los números se actualizan | TC-API-057 | Pasó |
| HU-P05 | CA-P05-01 Hotel publicado | TC-API-059, TC-WEB-024, TC-MOB-026 | Pasó |
| HU-P05 | CA-P05-02 Máximo 5 imágenes | TC-API-060 | Pasó |
| HU-P05 | CA-P05-03 Todo o nada | TC-API-084 | Pasó |
| HU-P06 | CA-P06-01 Cambios sin afectar reservas | TC-API-083 | Pasó |
| HU-P06 | CA-P06-02 Datos inválidos o hotel ajeno | TC-API-061 | Pasó |
| HU-P07 | CA-P07-01 Agregar o editar | TC-WEB-025, TC-MOB-027 | Pasó |
| HU-P07 | CA-P07-02 No bajar de lo reservado | TC-API-062 | Pasó |
| HU-P07 | CA-P07-03 Eliminar solo si no hay reservas | TC-API-063 | Pasó |
| HU-P07 | CA-P07-04 El precio de reservas pasadas no cambia | TC-API-064 | Pasó |
| HU-P07 | CA-P07-05 Datos inválidos o tipo ajeno | TC-API-085 | Pasó |
| HU-P08 | CA-P08-01 Tour publicado | TC-API-065, TC-WEB-026, TC-MOB-028 | Pasó |
| HU-P08 | CA-P08-02 Se respetan los límites | TC-API-067 | Pasó |
| HU-P09 | CA-P09-01 Cambios sin afectar reservas | TC-API-066 | Pasó |
| HU-P09 | CA-P09-02 Datos inválidos o tour ajeno | TC-API-067 | Pasó |
| HU-P10 | CA-P10-01 Programa en orden | TC-WEB-027 | Pasó |
| HU-P10 | CA-P10-02 Agregar al final | TC-API-068, TC-WEB-027, TC-MOB-029 | Pasó |
| HU-P10 | CA-P10-03 Quitar una actividad | TC-WEB-027 | Pasó |
| HU-P10 | CA-P10-04 Datos inválidos o tour ajeno | TC-API-069 | Pasó |
| HU-P11 | CA-P11-01 Nueva salida | TC-WEB-028, TC-MOB-030 | Pasó |
| HU-P11 | CA-P11-02 Salida repetida o pasada | TC-API-070 | Pasó |
| HU-P11 | CA-P11-03 No bajar de lo reservado | TC-API-071 | Pasó |
| HU-P11 | CA-P11-04 Cerrar o cancelar | TC-API-072, TC-WEB-028, TC-MOB-030 | Pasó |
| HU-P11 | CA-P11-05 Eliminar solo si no hay reservas | TC-WEB-028 | Pasó |
| HU-P11 | CA-P11-06 Lo mismo en web y app | TC-WEB-028, TC-MOB-030 | Pasó |
| HU-P12 | CA-P12-01 Listado completo | TC-API-073, TC-WEB-029, TC-MOB-031 | Pasó |
| HU-P12 | CA-P12-02 Filtrar pendientes | TC-API-074, TC-WEB-029 | Pasó |
| HU-P12 | CA-P12-03 Tours sin aprobación | TC-API-073 | Pasó |
| HU-P12 | CA-P12-04 Vacío no es lo mismo que un error | TC-WEB-032 | Pasó |
| HU-P12 | CA-P12-05 Reserva de otro proveedor | TC-API-075 | Pasó |
| HU-P13 | CA-P13-01 Aceptar | TC-API-031, TC-WEB-013, TC-MOB-015 | Pasó |
| HU-P13 | CA-P13-02 Si no decido, sigue pendiente | TC-WEB-013 | Pasó |
| HU-P13 | CA-P13-03 No se decide dos veces | TC-API-033 | Pasó |
| HU-P13 | CA-P13-04 Solo mis solicitudes | TC-API-034 | Pasó |
| HU-P14 | CA-P14-01 Rechazar libera las habitaciones | TC-API-032, TC-WEB-014, TC-MOB-016 | Pasó |
| HU-P14 | CA-P14-02 Motivo obligatorio | TC-API-035 | Pasó |
| HU-P14 | CA-P14-03 El viajero ve el motivo | TC-WEB-014, TC-MOB-016 | Pasó |
| HU-P15 | CA-P15-01 Me arrepiento | TC-WEB-030 | Pasó |
| HU-P15 | CA-P15-02 Eliminación permitida | TC-WEB-030, TC-MOB-032 | Pasó |
| HU-P15 | CA-P15-03 El historial se conserva | TC-API-078 | Pasó |
| HU-P15 | CA-P15-04 Publicación ajena o con reservas activas | TC-API-076, TC-API-077 | Pasó |
| HU-P15 | CA-P15-05 Eliminar dos veces | TC-API-086 | Pasó |

«Resultado» de un criterio es **Pasó** cuando al menos uno de sus casos pasó en la última corrida; **Pendiente** cuando sus casos solo son de Appium, JMeter o manuales y aún no se ejecutaron.

## Cómo se actualiza

1. Ejecutar las suites (`qa\ejecutar-todo.bat`, con `jmeter appium` para incluirlas).
2. `qa/trazabilidad/recolectar_resultados.py` lee los reportes y `generar_trazabilidad.py` regenera `qa/trazabilidad/salida/`.
3. La matriz con los resultados de cada nueva corrida queda en `qa/trazabilidad/salida/matriz_trazabilidad.html`; este documento es la foto de la entrega.
