# Documentación de casos de pruebas automatizadas — Trouvels360

## 1. Alcance y propósito

Este documento presenta los **157 casos automatizados** de Trouvels360: 86 de API con Karate, 33 de web con Selenium, 32 de móvil con Appium y 6 de rendimiento y concurrencia con JMeter. Los 136 escenarios de Karate incluyen variantes de datos de esos 86 casos. La documentación de funcionalidades se desarrolla por separado. Los tres casos manuales están fuera de este alcance.

El catálogo identifica el objetivo y la comprobación esperada, sus historias y criterios, la fuente automatizada y el último resultado registrado. Una prueba asociada a varias historias se cuenta una sola vez. La implementación enlazada contiene el detalle completo de los pasos y aserciones; la sección 4 explica cuatro casos representativos.

**Resultados comprobados.** Cada caso automatizado tiene evidencia aprobada del 12, 14 o 25 de noviembre de 2026. En la corrida de referencia de Appium pasaron 26 de 32 casos y las repeticiones completaron los seis restantes. El 25/11 se ejecutaron casos seleccionados de las cuatro suites en Kiri. Consulta el [detalle por suite](estrategia-de-pruebas.md#10-estado-actual).

## 2. Entorno y datos

- Aplicación bajo prueba: web Angular, API Laravel, FastAPI, MySQL y aplicación Android. Kiri ejecuta la web, las API y la base de datos.
- Web: `http://localhost:4200`; API: `http://localhost:8000/api`; servicio de itinerarios: `http://localhost:8001/api/health`.
- Para Java: JDK 21 o 17 y Maven. Selenium necesita navegador compatible; Appium requiere servidor Appium 2, UiAutomator2, SDK, dispositivo Android y APK; JMeter requiere JMeter 5.6.3.
- Las suites crean usuarios, hoteles, tours y reservas; algunas usan datos del `QaSeeder`. La app Android del emulador accede a la API mediante `10.0.2.2:8000`.
- Ejecutar en un entorno de prueba. Revisar los límites de registro e inicio de sesión antes de la carga. No reiniciar la base sin conservar antes los datos necesarios.

## 3. Procedimiento de ejecución y evidencias

### API — Karate

1. Comprobar la API y, para itinerarios, FastAPI. Desde `qa/karate`, ejecutar `mvn test`.
2. La suite prepara datos, invoca servicios y valida códigos HTTP, cuerpos, esquemas, tiempos y reglas aplicables. El detalle de cada escenario está en el archivo `.feature`, sus helpers y CSV.
3. Revisar `qa/karate/target/karate-reports/karate-summary.html` y el reporte del caso. Registrar fecha y resultado.

### Web — Selenium

1. Comprobar web, API, JDK, Maven y navegador. Desde `qa/selenium`, ejecutar `mvn test`; con `mvn test -Dheadless=false` se muestra el navegador.
2. La suite prepara datos por API, usa selectores `data-testid` y esperas explícitas, y comprueba los resultados de los flujos.
3. Revisar `qa/selenium/target/reporte-selenium/index.html`, capturas y resultados JUnit. En algunos flujos la sesión se prepara por API; las pruebas específicas de login y registro sí ejercitan los formularios.

### Móvil — Appium

1. Comprobar APIs, APK, Appium y dispositivo en estado `device` con `adb devices`; revisar `qa/appium/src/test/resources/config.properties` y el formato de fecha del emulador.
2. Desde `qa/appium`, ejecutar `mvn test`. La suite crea datos por API e interactúa con la app mediante UiAutomator2.
3. Revisar `qa/appium/target/reporte-appium/index.html` y sus capturas. Conservar el reporte de cada repetición y consultar la matriz para identificar la evidencia histórica de cada caso.

### Rendimiento — JMeter

1. Comprobar API, base, usuarios de prueba, Java y JMeter. El plan usa archivos CSV y crea recursos para los escenarios de concurrencia.
2. Desde `qa/jmeter`, ejecutar `ejecutar.bat`. **Este script elimina la carpeta `resultados` al empezar:** guardar una copia si sus reportes históricos son necesarios.
3. Revisar `resultados/reporte-html/index.html`, el JTL y `resultados/verificacion.txt`; comparar p95, porcentaje de errores y conteos con los umbrales definidos. Si se usa una carga abreviada para la exposición, identificarla como tal y no atribuirle los resultados de la carga completa.

## 4. Fichas detalladas para la exposición

### TC-API-031 — Aceptación de una reserva por API

- **Herramienta e historias:** Karate; HU-V12 y HU-P13. **Fuente:** `qa/karate/src/test/java/trouvels360/aprobacion.feature`.
- **Precondiciones y datos:** API y base disponibles; sesiones de prueba de viajero y proveedor. El escenario crea un hotel con tres habitaciones y fechas libres.
- **Pasos:** 1) Crear la solicitud como viajero y guardar su identificador. 2) Como proveedor, enviar la aceptación. 3) Como viajero, consultar esa reserva.
- **Resultado esperado:** creación HTTP 201; aceptación HTTP 200 con estado `confirmada` y fecha de decisión; consulta HTTP 200 con el mismo estado. Se valida el umbral de tiempo configurado para la aceptación.
- **Evidencia:** aprobado el 25/11/2026 en Kiri. [Reporte Karate del escenario](../../qa/karate/target/karate-reports/trouvels360.aprobacion.html).

### TC-WEB-013 — Aprobación en dos navegadores

- **Herramienta e historias:** Selenium; HU-V12 y HU-P13. **Fuente:** `qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java`.
- **Precondiciones y datos:** web y API disponibles. La prueba prepara por API proveedor, hotel, viajero y reserva pendiente.
- **Pasos:** 1) En el navegador del viajero, comprobar que la reserva aparece pendiente. 2) En otro navegador, abrir la bandeja del proveedor y aceptar la solicitud. 3) Recargar las reservas del viajero.
- **Resultado esperado:** la misma reserva pasa de `pendiente` a `CONFIRMADA`.
- **Evidencia:** reporte Selenium, resultados JUnit y capturas. El acceso se prepara por API; no es una prueba del formulario de login.

### TC-MOB-015 — Aceptación desde Android

- **Herramienta e historia:** Appium; HU-P13. **Fuente:** `qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java`.
- **Precondiciones y datos:** APK, Appium, dispositivo y API disponibles. La prueba crea el hotel y una solicitud pendiente por API.
- **Pasos:** 1) Iniciar sesión como proveedor en la app. 2) Abrir solicitudes recibidas, filtrar pendientes y aceptar por código. 3) Verificar el estado mediante la API. 4) Entrar como viajero y abrir sus reservas.
- **Resultado esperado:** aparece el mensaje de aceptación, la API devuelve `confirmada` y el viajero ve la solicitud confirmada.
- **Evidencia:** aprobado en las repeticiones del 14/11/2026 y en Kiri el 25/11/2026. [Reporte Appium y capturas](../../qa/appium/target/reporte-demo-formularios/index.html).

### TC-PERF-003 — Solicitudes por la última habitación

- **Herramienta e historia:** JMeter; HU-V07. **Fuente:** `qa/jmeter/trouvels360.jmx`.
- **Precondiciones y datos:** API y base disponibles; el grupo de preparación crea un hotel con una unidad.
- **Pasos:** 1) Preparar y sincronizar 20 hilos. 2) Enviar solicitudes simultáneas por la última unidad. 3) Revisar respuestas y verificación final.
- **Resultado esperado:** una respuesta 201, diecinueve respuestas 409, ninguna otra respuesta y verificación final satisfactoria.
- **Evidencia:** el 12/11/2026 y el 25/11/2026 en Kiri se registraron un HTTP 201 y diecinueve HTTP 409, con verificación final satisfactoria. [Verificación del 25/11/2026](../../qa/jmeter/resultados-verificacion-expo-20261125-191749/verificacion.txt).

## 5. Catálogo completo de casos automatizados

El título resume la comprobación esperada. La fuente automatizada contiene el procedimiento, los datos y las aserciones de cada caso. El catálogo indica el último resultado registrado al 25/11/2026. La [matriz generada](../../qa/trazabilidad/salida/matriz_trazabilidad.html) detalla los resultados por historia y criterio.

### 5.1 API — Karate

| Caso | Objetivo y resultado esperado | Historias / criterios | Fuente automatizada | Resultado registrado |
|---|---|---|---|---|
| TC-API-001 | Registro viajero válido devuelve 201 y esquema de usuario sin password | HU-V01<br>CA-V01-01 | [`qa/karate/src/test/java/trouvels360/auth.feature:9`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-25 06:56:18 p. m. |
| TC-API-002 | Data-driven (CSV) de campos inválidos devuelve 422 con error por campo | HU-V01<br>CA-V01-02, CA-V01-03 | [`qa/karate/src/test/java/trouvels360/auth.feature:18`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-25 06:56:18 p. m. |
| TC-API-003 | Correo duplicado en mayúsculas devuelve 422 | HU-P01, HU-V01<br>CA-P01-04, CA-V01-04 | [`qa/karate/src/test/java/trouvels360/auth.feature:33`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-004 | Rol 'admin' devuelve 422 | HU-V01<br>CA-V01-05 | [`qa/karate/src/test/java/trouvels360/auth.feature:43`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-005 | Registro proveedor normaliza '987654321' a '+51 987654321' | HU-P01<br>CA-P01-01 | [`qa/karate/src/test/java/trouvels360/auth.feature:53`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-006 | Data-driven de RUC inválidos y duplicado (422) | HU-P01<br>CA-P01-02 | [`qa/karate/src/test/java/trouvels360/auth.feature:62`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-007 | Teléfonos inválidos devuelven 422 con mensaje | HU-P01<br>CA-P01-03 | [`qa/karate/src/test/java/trouvels360/auth.feature:76`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-008 | Login devuelve 200, cookie access_token HttpOnly y esquema de usuario | HU-P02, HU-V02<br>CA-P02-01, CA-V02-01 | [`qa/karate/src/test/java/trouvels360/auth.feature:89`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-009 | POST sin X-XSRF-TOKEN devuelve 403 | HU-V02<br>CA-V02-06 | [`qa/karate/src/test/java/trouvels360/auth.feature:107`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-010 | Credenciales inválidas 401 y faltantes 422 | HU-V02<br>CA-V02-03 | [`qa/karate/src/test/java/trouvels360/auth.feature:114`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-011 | Token reutilizado tras logout devuelve 401 | HU-V02<br>CA-V02-04, CA-V02-05 | [`qa/karate/src/test/java/trouvels360/auth.feature:129`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-012 | Viajero en ruta de proveedor devuelve 403 | HU-P02<br>CA-P02-02 | [`qa/karate/src/test/java/trouvels360/auth.feature:145`](../../qa/karate/src/test/java/trouvels360/auth.feature) | Pasó<br>2026-11-12 05:58:36 p. m. |
| TC-API-013 | PATCH /usuarios/me actualiza y GET /auth/me devuelve lo mismo | HU-V03<br>CA-V03-01 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:10`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-014 | Avatar y teléfono inválidos devuelven 422 | HU-P02, HU-V03<br>CA-P02-03, CA-V03-02 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:24`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-015 | Enviar rol devuelve 422 | HU-V03<br>CA-V03-03 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:39`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-016 | Cambio de contraseña: login con nueva 200 y con anterior 401 | HU-V03<br>CA-V03-04, CA-V03-05 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:51`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-017 | DELETE con reserva activa devuelve 409 | HU-V04<br>CA-V04-03 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:65`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-018 | DELETE sin reservas devuelve 200 y login posterior 401 | HU-V04<br>CA-V04-02 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:78`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-019 | Reservas de la cuenta eliminada siguen visibles para el proveedor | HU-V04<br>CA-V04-04 | [`qa/karate/src/test/java/trouvels360/cuenta.feature:89`](../../qa/karate/src/test/java/trouvels360/cuenta.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-020 | Búsqueda válida: todos los resultados cumplen ciudad, capacidad y unidades (match each) | HU-V05<br>CA-V05-01 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:13`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-021 | Data-driven de fechas y cantidades inválidas devuelve 422 | HU-V05<br>CA-V05-03, CA-V05-04 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:29`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-022 | Hotel con unidades ocupadas por reservas pendientes no aparece | HU-V05<br>CA-V05-02 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:41`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-023 | Esquema JSON del detalle de hotel (habitaciones, calificación, reseñas) | HU-V06<br>CA-V06-01 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:61`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-024 | Cotización data-driven (CSV) con tarifas, noches y unidades → subtotal, igv, total exactos | HU-V06<br>CA-V06-03 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:76`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-025 | Cotizar más unidades que las libres devuelve 422 | HU-V06<br>CA-V06-04 | [`qa/karate/src/test/java/trouvels360/hoteles.feature:94`](../../qa/karate/src/test/java/trouvels360/hoteles.feature) | Pasó<br>2026-11-12 05:58:38 p. m. |
| TC-API-026 | Reserva válida 201: estado pendiente, código único, subtotal/igv/total = cotización | HU-V07<br>CA-V07-01 | [`qa/karate/src/test/java/trouvels360/reservas-hotel.feature:11`](../../qa/karate/src/test/java/trouvels360/reservas-hotel.feature) | Pasó<br>2026-11-12 06:00:19 p. m. |
| TC-API-027 | Data-driven de tarjetas inválidas (Luhn, vencida, CVC) → 422 sin reserva creada | HU-V07<br>CA-V07-02 | [`qa/karate/src/test/java/trouvels360/reservas-hotel.feature:28`](../../qa/karate/src/test/java/trouvels360/reservas-hotel.feature) | Pasó<br>2026-11-12 06:00:19 p. m. |
| TC-API-028 | Disponibilidad tras reservar descuenta las unidades | HU-V07<br>CA-V07-03 | [`qa/karate/src/test/java/trouvels360/reservas-hotel.feature:52`](../../qa/karate/src/test/java/trouvels360/reservas-hotel.feature) | Pasó<br>2026-11-12 06:00:19 p. m. |
| TC-API-029 | Varios tipos con uno sin stock → 409 y ninguno registrado | HU-V07<br>CA-V07-04 | [`qa/karate/src/test/java/trouvels360/reservas-hotel.feature:70`](../../qa/karate/src/test/java/trouvels360/reservas-hotel.feature) | Pasó<br>2026-11-12 06:00:19 p. m. |
| TC-API-030 | Mismo Idempotency-Key devuelve la misma solicitud | HU-V07<br>CA-V07-06 | [`qa/karate/src/test/java/trouvels360/reservas-hotel.feature:87`](../../qa/karate/src/test/java/trouvels360/reservas-hotel.feature) | Pasó<br>2026-11-12 06:00:19 p. m. |
| TC-API-031 | Flujo encadenado (call): viajero reserva → proveedor acepta → viajero ve confirmada | HU-P13, HU-V12<br>CA-P13-01, CA-V12-01 | [`qa/karate/src/test/java/trouvels360/aprobacion.feature:14`](../../qa/karate/src/test/java/trouvels360/aprobacion.feature) | Pasó<br>2026-11-25 06:56:17 p. m. |
| TC-API-032 | Rechazo libera unidades: disponibilidad antes y después | HU-P14<br>CA-P14-01 | [`qa/karate/src/test/java/trouvels360/aprobacion.feature:31`](../../qa/karate/src/test/java/trouvels360/aprobacion.feature) | Pasó<br>2026-11-12 05:57:51 p. m. |
| TC-API-033 | Aceptar dos veces → 409; aceptar una cancelada → 409 | HU-P13<br>CA-P13-03 | [`qa/karate/src/test/java/trouvels360/aprobacion.feature:54`](../../qa/karate/src/test/java/trouvels360/aprobacion.feature) | Pasó<br>2026-11-12 05:57:51 p. m. |
| TC-API-034 | Proveedor B sobre reserva de proveedor A → 403 | HU-P13<br>CA-P13-04 | [`qa/karate/src/test/java/trouvels360/aprobacion.feature:77`](../../qa/karate/src/test/java/trouvels360/aprobacion.feature) | Pasó<br>2026-11-12 05:57:51 p. m. |
| TC-API-035 | Rechazo sin motivo → 422 | HU-P14<br>CA-P14-02 | [`qa/karate/src/test/java/trouvels360/aprobacion.feature:90`](../../qa/karate/src/test/java/trouvels360/aprobacion.feature) | Pasó<br>2026-11-12 05:57:51 p. m. |
| TC-API-036 | Filtros combinados: todos los resultados cumplen destino y categoría (match each) | HU-V08<br>CA-V08-01 | [`qa/karate/src/test/java/trouvels360/tours.feature:10`](../../qa/karate/src/test/java/trouvels360/tours.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-037 | Data-driven de filtros inválidos → 422 | HU-V08<br>CA-V08-02 | [`qa/karate/src/test/java/trouvels360/tours.feature:29`](../../qa/karate/src/test/java/trouvels360/tours.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-038 | Esquema del detalle; ninguna salida devuelta es pasada ni no programada | HU-V09<br>CA-V09-01 | [`qa/karate/src/test/java/trouvels360/tours.feature:41`](../../qa/karate/src/test/java/trouvels360/tours.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-039 | Cotización de tour data-driven (precio × personas + 18%) | HU-V09<br>CA-V09-02 | [`qa/karate/src/test/java/trouvels360/tours.feature:62`](../../qa/karate/src/test/java/trouvels360/tours.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-040 | Cotizar más personas que el cupo → 422 | HU-V09<br>CA-V09-03 | [`qa/karate/src/test/java/trouvels360/tours.feature:76`](../../qa/karate/src/test/java/trouvels360/tours.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-041 | Reserva 201: estado confirmada, total = cotización, cupo_reservado += personas | HU-V10, HU-V12<br>CA-V10-01, CA-V12-03 | [`qa/karate/src/test/java/trouvels360/reservas-tour.feature:11`](../../qa/karate/src/test/java/trouvels360/reservas-tour.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-042 | Salida cerrada / pasada / sin cupo → 409 o 422 y cupos sin cambio | HU-V10<br>CA-V10-03 | [`qa/karate/src/test/java/trouvels360/reservas-tour.feature:27`](../../qa/karate/src/test/java/trouvels360/reservas-tour.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-043 | Idempotencia: dos envíos, una sola reserva y un solo descuento | HU-V10<br>CA-V10-05 | [`qa/karate/src/test/java/trouvels360/reservas-tour.feature:47`](../../qa/karate/src/test/java/trouvels360/reservas-tour.feature) | Pasó<br>2026-11-12 06:00:20 p. m. |
| TC-API-044 | Esquema paginado (data, meta.total, meta.last_page) y solo reservas propias | HU-V11<br>CA-V11-01, CA-V11-02 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:20`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-045 | Estado tras aceptar y tras rechazar (con motivo) | HU-V12<br>CA-V12-01, CA-V12-02 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:49`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-046 | Reserva ajena → 404 | HU-V11<br>CA-V11-03 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:73`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-047 | Cancelar hotel: estado cancelada y unidades liberadas | HU-V13<br>CA-V13-02 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:85`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-048 | Cancelar tour devuelve exactamente las personas al cupo | HU-V13<br>CA-V13-03 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:98`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-049 | Cancelar dos veces → 409; cancelar rechazada → 409; ajena → 404 | HU-V13<br>CA-V13-04, CA-V13-05 | [`qa/karate/src/test/java/trouvels360/mis-reservas.feature:113`](../../qa/karate/src/test/java/trouvels360/mis-reservas.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-050 | Resumen: promedio y total coinciden con el conjunto completo | HU-P04, HU-V14<br>CA-P04-02, CA-V14-01 | [`qa/karate/src/test/java/trouvels360/resenas.feature:27`](../../qa/karate/src/test/java/trouvels360/resenas.feature) | Pasó<br>2026-11-12 06:00:18 p. m. |
| TC-API-051 | Publicar con reserva finalizada 201; sin reserva 403 con mensaje | HU-V14<br>CA-V14-02, CA-V14-03 | [`qa/karate/src/test/java/trouvels360/resenas.feature:62`](../../qa/karate/src/test/java/trouvels360/resenas.feature) | Pasó<br>2026-11-12 06:00:18 p. m. |
| TC-API-052 | Data-driven de longitudes de comentario y calificaciones fuera de rango → 422 | HU-V14<br>CA-V14-03 | [`qa/karate/src/test/java/trouvels360/resenas.feature:85`](../../qa/karate/src/test/java/trouvels360/resenas.feature) | Pasó<br>2026-11-12 06:00:18 p. m. |
| TC-API-053 | Editar/eliminar reseña ajena → 403; segunda reseña → 409 | HU-V14<br>CA-V14-04, CA-V14-05 | [`qa/karate/src/test/java/trouvels360/resenas.feature:105`](../../qa/karate/src/test/java/trouvels360/resenas.feature) | Pasó<br>2026-11-12 06:00:18 p. m. |
| TC-API-054 | Itinerario válido: esquema de días y actividades | HU-V15<br>CA-V15-01 | [`qa/karate/src/test/java/trouvels360/itinerario.feature:11`](../../qa/karate/src/test/java/trouvels360/itinerario.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-055 | Interés inválido → 422 | HU-V15<br>CA-V15-04 | [`qa/karate/src/test/java/trouvels360/itinerario.feature:31`](../../qa/karate/src/test/java/trouvels360/itinerario.feature) | Pasó<br>2026-11-12 05:58:39 p. m. |
| TC-API-056 | Paginación: meta.total, last_page y página 2 con los registros 16+ | HU-P03<br>CA-P03-01 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:19`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-057 | Indicadores por estado tras crear reservas en 4 estados | HU-P04<br>CA-P04-01, CA-P04-03 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:54`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-058 | Viajero en /proveedor/servicios → 403 | HU-P03<br>CA-P03-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:94`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-059 | Crear hotel con habitaciones y verificar en el catálogo | HU-P05<br>CA-P05-01 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:112`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-060 | 6 imágenes → 422 | HU-P05<br>CA-P05-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:137`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-061 | Editar hotel ajeno → 403 | HU-P06<br>CA-P06-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:147`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-062 | Reducir unidades bajo ocupación → 409 con mínimo | HU-P07<br>CA-P07-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:166`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-063 | Eliminar con reservas → 409; sin reservas → 204 | HU-P07<br>CA-P07-03 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:185`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-064 | Cambio de tarifa no altera total de reservas previas | HU-P07<br>CA-P07-04 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:209`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-065 | Crear tour y verificar en catálogo | HU-P08<br>CA-P08-01 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:233`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-066 | Editar precio: ids de salidas y totales de reservas sin cambio | HU-P09<br>CA-P09-01 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:258`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-067 | Data-driven de límites (imágenes, items, categoría) → 422 | HU-P08, HU-P09<br>CA-P08-02, CA-P09-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:282`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-068 | Dos actividades sin orden reciben 1 y 2 | HU-P10<br>CA-P10-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:303`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-069 | Actividad en tour ajeno → 403 | HU-P10<br>CA-P10-04 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:328`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-070 | Salida duplicada → 409; fecha pasada → 422 | HU-P11<br>CA-P11-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:342`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-071 | cupo_total menor a reservado → 422; cupo_reservado enviado se ignora | HU-P11<br>CA-P11-03 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:360`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-072 | Salida cerrada rechaza reservas (call a reserva de viajero) | HU-P11<br>CA-P11-04 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:377`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-073 | Solo reservas de servicios propios (match each servicio.proveedor_id) | HU-P12<br>CA-P12-01, CA-P12-03 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:397`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-074 | Filtro estado=pendiente | HU-P12<br>CA-P12-02 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:422`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-075 | Detalle ajeno → 404 | HU-P12<br>CA-P12-05 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:450`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-076 | Eliminar servicio ajeno → 403 (seguridad) | HU-P15<br>CA-P15-04 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:466`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-077 | Con reserva activa → 409 | HU-P15<br>CA-P15-04 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:489`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-078 | Tras eliminar, GET /mis-reservas del viajero sigue 200 con el nombre del servicio | HU-P15<br>CA-P15-03 | [`qa/karate/src/test/java/trouvels360/proveedor.feature:504`](../../qa/karate/src/test/java/trouvels360/proveedor.feature) | Pasó<br>2026-11-12 05:59:31 p. m. |
| TC-API-079 | Cambiar la fecha de salida recalcula el total y no crea reserva | HU-V06<br>CA-V06-05 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:13`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-080 | Tour sin salidas disponibles: detalle sin salidas y cotizar 409 | HU-V09<br>CA-V09-04 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:35`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-081 | Pago de tour inválido (CSV) no crea reserva ni descuenta cupos | HU-V10<br>CA-V10-02 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:53`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-082 | Salida cancelada por el proveedor: reserva confirmada con aviso | HU-V12<br>CA-V12-04 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:69`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-083 | Editar descripción e imágenes conserva id y reservas | HU-P06<br>CA-P06-01 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:86`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-084 | Tipo de habitación inválido: no queda hotel a medias | HU-P05<br>CA-P05-03 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:109`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-085 | Tarifa 0 → 422 y tipo ajeno → 403 sin cambios | HU-P07<br>CA-P07-05 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:129`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |
| TC-API-086 | Repetir la eliminación devuelve 404 sin efectos | HU-P15<br>CA-P15-05 | [`qa/karate/src/test/java/trouvels360/cobertura-adicional.feature:149`](../../qa/karate/src/test/java/trouvels360/cobertura-adicional.feature) | Pasó<br>2026-11-12 05:58:37 p. m. |

### 5.2 Web — Selenium

| Caso | Objetivo y resultado esperado | Historias / criterios | Fuente automatizada | Resultado registrado |
|---|---|---|---|---|
| TC-WEB-001 | Registro de viajero end-to-end y aterrizaje en catálogo | HU-V01<br>CA-V01-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:27`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-002 | Mensajes de validación en el formulario | HU-P01, HU-V01<br>CA-P01-04, CA-V01-02, CA-V01-03 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:46`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-003 | Registro de proveedor y acceso al panel del proveedor | HU-P01<br>CA-P01-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:73`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-004 | Login viajero y proveedor muestran el menú de su rol | HU-P02, HU-V02<br>CA-P02-01, CA-P02-02, CA-V02-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:89`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-005 | Login desde 'Reservar' vuelve al detalle con la selección | HU-V02<br>CA-V02-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:114`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-006 | Editar perfil y ver el nombre actualizado en el menú | HU-V03<br>CA-V03-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:136`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-007 | Diálogo de confirmación: cancelar conserva la cuenta | HU-V04<br>CA-V04-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java:150`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AutenticacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-008 | Búsqueda en Cusco y validación de tarjetas de resultado | HU-V05<br>CA-V05-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:30`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-009 | Validación de fechas pasadas en el buscador | HU-V05<br>CA-V05-03 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:50`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-010 | Selección de 2 tipos y verificación del resumen con IGV | HU-V06<br>CA-V06-02, CA-V06-03 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:66`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-011 | Flujo completo búsqueda → selección → pago → comprobante PENDIENTE | HU-V07<br>CA-V07-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:83`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java) | Pasó<br>2026-11-25 19:02 |
| TC-WEB-012 | Validaciones del formulario de pago | HU-V07<br>CA-V07-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java:114`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/HotelesTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-013 | Proveedor acepta desde el panel y el viajero ve CONFIRMADA (dos navegadores) | HU-P13, HU-V12<br>CA-P13-01, CA-P13-02, CA-V12-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:26`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-014 | Proveedor rechaza con motivo y el viajero ve RECHAZADA | HU-P14, HU-V12<br>CA-P14-01, CA-P14-03, CA-V12-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java:55`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/AprobacionTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-015 | Filtrar por categoría y destino | HU-V08<br>CA-V08-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:28`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-016 | Elegir salida y personas, verificar total con IGV | HU-V09<br>CA-V09-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:41`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-017 | Reserva de tour end-to-end con comprobante CONFIRMADA | HU-V10<br>CA-V10-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java:56`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ToursTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-018 | Mis reservas: pestañas hotel/tour, filtro por estado y paginación | HU-V11<br>CA-V11-01, CA-V11-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:26`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-019 | Reserva rechazada muestra motivo | HU-V12<br>CA-V12-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:59`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-020 | Cancelar desde Mis reservas con diálogo de confirmación | HU-V13<br>CA-V13-01, CA-V13-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java:76`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/MisReservasTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-021 | Publicar, editar y eliminar reseña en el detalle | HU-V14<br>CA-V14-02, CA-V14-04 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:30`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-022 | Generar itinerario desde el detalle del hotel | HU-V15<br>CA-V15-01, CA-V15-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java:60`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ResenasItinerarioTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-023 | Panel: paginación y tarjetas de indicadores | HU-P03, HU-P04<br>CA-P03-01, CA-P04-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:29`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-024 | Crear hotel desde el formulario del proveedor | HU-P05<br>CA-P05-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:56`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-025 | Agregar y editar tipo de habitación | HU-P07<br>CA-P07-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:75`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-026 | Crear tour con salida desde el formulario | HU-P08<br>CA-P08-01 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:96`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-027 | Agregar, reordenar y eliminar actividades | HU-P10<br>CA-P10-01, CA-P10-02, CA-P10-03 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:118`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-028 | Crear, cerrar y eliminar salida desde el panel | HU-P11<br>CA-P11-01, CA-P11-04, CA-P11-05, CA-P11-06 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:140`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-029 | Bandeja de reservas con filtros y paginación | HU-P12<br>CA-P12-01, CA-P12-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:162`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-030 | Eliminar publicación con diálogo de confirmación | HU-P15<br>CA-P15-01, CA-P15-02 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java:198`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/ProveedorTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-031 | Volver desde el detalle conserva la búsqueda (hoteles y tours) | HU-V05, HU-V08<br>CA-V05-06, CA-V08-04 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:28`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-032 | Sin resultados vs. error con Reintentar (tours y bandeja del proveedor) | HU-P12, HU-V08<br>CA-P12-04, CA-V08-03 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:59`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java) | Pasó<br>2026-11-12 18:01 |
| TC-WEB-033 | Sesión vencida: 401, login y regreso a la página | HU-V02<br>CA-V02-04 | [`qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java:80`](../../qa/selenium/src/test/java/com/trouvels360/web/pruebas/CoberturaAdicionalTest.java) | Pasó<br>2026-11-12 18:01 |

### 5.3 Móvil — Appium

| Caso | Objetivo y resultado esperado | Historias / criterios | Fuente automatizada | Resultado registrado |
|---|---|---|---|---|
| TC-MOB-001 | Registro de viajero desde la app y sesión iniciada | HU-V01<br>CA-V01-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:23`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-25 18:01 |
| TC-MOB-002 | Botón Registrarme deshabilitado con contraseñas distintas | HU-P01, HU-V01<br>CA-P01-04, CA-V01-03 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:41`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-25 18:01 |
| TC-MOB-003 | Registro de proveedor desde la app y acceso al panel | HU-P01<br>CA-P01-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:55`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-25 18:01 |
| TC-MOB-004 | Login, cierre de sesión y bloqueo de pestañas privadas | HU-V02<br>CA-V02-01, CA-V02-03, CA-V02-05 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:70`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-25 18:01 |
| TC-MOB-005 | Reservar sin sesión → login → regreso al detalle | HU-V02<br>CA-V02-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:95`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-25 18:04 |
| TC-MOB-006 | Editar perfil en la app y verificarlo en la pestaña Perfil | HU-V03<br>CA-V03-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:114`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-007 | Cambio de contraseña con confirmación distinta deshabilita Guardar | HU-V03<br>CA-V03-04 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:130`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-008 | Eliminar cuenta desde Perfil y volver a la pantalla de login | HU-V04<br>CA-V04-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java:151`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/CuentaMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-009 | Búsqueda por ciudad y fechas en la app | HU-V05<br>CA-V05-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:29`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-010 | Estado vacío vs error con Reintentar | HU-V05<br>CA-V05-05 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:42`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-14 02:35 |
| TC-MOB-011 | Selección de habitaciones y total con IGV en la app | HU-V06<br>CA-V06-02, CA-V06-03 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:71`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-012 | Botón + deshabilitado al llegar a las unidades disponibles | HU-V06<br>CA-V06-04 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:88`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-013 | Flujo completo de reserva de hotel en la app | HU-V07<br>CA-V07-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:102`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-25 18:04 |
| TC-MOB-014 | Formulario de pago rechaza tarjeta vencida | HU-V07<br>CA-V07-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java:126`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/HotelesMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-015 | Proveedor acepta una solicitud desde la app | HU-P13<br>CA-P13-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:25`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java) | Pasó<br>2026-11-25 18:01 |
| TC-MOB-016 | Proveedor rechaza con motivo desde la app | HU-P14<br>CA-P14-01, CA-P14-03 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java:55`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/AprobacionMovilTest.java) | Pasó<br>2026-11-14 02:29 |
| TC-MOB-017 | Chips de categoría y búsqueda por destino | HU-V08<br>CA-V08-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:27`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java) | Pasó<br>2026-11-14 02:29 |
| TC-MOB-018 | Elegir salida y personas en la app; + se deshabilita al tope de cupos | HU-V09<br>CA-V09-02, CA-V09-03 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:50`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-019 | Reserva de tour end-to-end en la app | HU-V10<br>CA-V10-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java:65`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ToursMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-020 | Mis reservas en la app coincide con la web (mismo código y total) | HU-V11<br>CA-V11-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:25`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-021 | Scroll infinito / paginación de reservas | HU-V11<br>CA-V11-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:45`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-022 | Cancelar desde la app y ver estado CANCELADA | HU-V13<br>CA-V13-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java:69`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ReservasMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-023 | Publicar reseña desde la app | HU-V14<br>CA-V14-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:21`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-024 | Generar itinerario en la app y continuar con la reserva | HU-V15<br>CA-V15-01, CA-V15-03 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java:46`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ResenasItinerarioMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-025 | Panel del proveedor en la app con indicadores | HU-P03, HU-P04<br>CA-P03-01, CA-P04-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:32`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-026 | Crear hotel desde la app del proveedor | HU-P05<br>CA-P05-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:51`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-027 | Agregar tipo de habitación en la app | HU-P07<br>CA-P07-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:72`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-028 | Crear tour desde la app | HU-P08<br>CA-P08-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:94`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-25 18:04 |
| TC-MOB-029 | Agregar actividad desde la app | HU-P10<br>CA-P10-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:118`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-030 | Crear y cerrar salida desde la app | HU-P11<br>CA-P11-01, CA-P11-04, CA-P11-06 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:133`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-031 | Bandeja de reservas en la app | HU-P12<br>CA-P12-01 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:152`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-14 01:59 |
| TC-MOB-032 | Eliminar publicación desde la app | HU-P15<br>CA-P15-02 | [`qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java:174`](../../qa/appium/src/test/java/com/trouvels360/movil/pruebas/ProveedorMovilTest.java) | Pasó<br>2026-11-25 18:35 |

### 5.4 Rendimiento — JMeter

| Caso | Objetivo y resultado esperado | Historias / criterios | Fuente automatizada | Resultado registrado |
|---|---|---|---|---|
| TC-PERF-001 | Login con 50 usuarios concurrentes desde CSV: p95 < 800 ms, 0% errores | HU-V02<br>CA-V02-07 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-25 19:18 |
| TC-PERF-002 | Carga de catálogo: 100 usuarios, ramp-up 60 s, ciudades desde CSV, p95 < 1 s | HU-V05<br>CA-V05-07 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-12 18:36 |
| TC-PERF-003 | 20 hilos simultáneos (Synchronizing Timer) por la última unidad: exactamente 1×201 y 19×409 | HU-V07<br>CA-V07-05 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-25 19:18 |
| TC-PERF-004 | Carga de búsqueda de tours: 100 usuarios, categorías desde CSV, p95 < 1 s | HU-V08<br>CA-V08-05 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-12 18:36 |
| TC-PERF-005 | 30 hilos simultáneos sobre salida de 10 cupos: suma de personas confirmadas <= 10 | HU-V10<br>CA-V10-04 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-12 18:36 |
| TC-PERF-006 | Panel del proveedor con 30 usuarios concurrentes, p95 < 1,5 s | HU-P03<br>CA-P03-03 | [`qa/jmeter/trouvels360.jmx`](../../qa/jmeter/trouvels360.jmx) | Pasó<br>2026-11-12 18:36 |

## 6. Registro de una nueva ejecución

Registrar fecha y hora, entorno, versión probada, responsable, caso, datos, resultado real, estado (pasó/falló/bloqueado/no ejecutado), ruta de la evidencia y enlace al ticket o defecto cuando corresponda. Conservar evidencia de las repeticiones y sus resultados anteriores.

La [matriz](../../qa/trazabilidad/salida/matriz_trazabilidad.html), los reportes de las suites y el [registro de defectos](../3-trazabilidad/defectos.md) complementan este documento. La importación y vinculación de casos en Jira o Azure Boards debe comprobarse por separado.
