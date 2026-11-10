# Datos de prueba (seeder)

Se cargan con `php artisan migrate:fresh --seed`. Las fechas son relativas al día en que se ejecuta.

## Cuentas

| Rol | Correo | Contraseña | Uso |
| --- | --- | --- | --- |
| Viajero | `edwinviajero@gmail.com` | `viajero` | Demo: tiene reservas confirmadas pasadas y 2 solicitudes PENDIENTES |
| Proveedor | `edwinproveedor@gmail.com` | `proveedor` | Dueño de casi todo el catálogo y del Hotel/Tour QA |
| Proveedor | `qa.proveedor2@trouvels360.test` | `Proveedor123` | Pruebas de autorización (403) |
| Viajeros | `qa.viajero1` … `qa.viajero30@trouvels360.test` | `Viajero123` | Suites automatizadas y CSV de JMeter |

`qa.viajero1` tiene una estadía y un tour ya finalizados: puede publicar reseñas en **Hotel QA Concurrencia** y **Tour QA Cupos**.

## Servicios fijos

| Servicio | Datos clave | Para probar |
| --- | --- | --- |
| Hotel QA Concurrencia (Cusco) | Habitación Única: 1 unidad, S/ 100 · Doble QA: 5 u., S/ 150 · Suite QA: 2 u., S/ 320 | Última unidad (concurrencia), cotización 150 × 2 noches × 2 = 600 + 108 = **708** |
| Hotel QA Proveedor Dos (Lima) | Estándar: 3 u., S/ 90 | Hotel de otro proveedor |
| Tour QA Cupos (Cusco, Aventura, S/ 120) | Salida +10 días 09:00 (10 cupos, programada) · +12 días (cerrada) · +14 días 15:00 (30 cupos) · −5 días (pasada) | Carrera por cupos, salida cerrada, salida pasada, cotización 120 × 3 = 360 + 64.80 = **424.80** |

## Tarjetas para el pago simulado

| Caso | Número | Vencimiento | CVC |
| --- | --- | --- | --- |
| Válida | `4242 4242 4242 4242` | cualquier MM/AA futuro | `123` |
| Válida | `5555 5555 5555 4444` | cualquier MM/AA futuro | `123` |
| Luhn inválido | `4242 4242 4242 4241` | — | — |
| Vencida | — | `01/20` | — |
