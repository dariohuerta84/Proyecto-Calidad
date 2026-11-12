# Pruebas E2E web — Selenium 4 + JUnit 5 (QA-03)

33 casos (**TC-WEB-001 a TC-WEB-033**: 30 de la etapa 1 y 3 agregados para criterios sin caso) sobre la web Angular, con Page Object Model, selectores `data-testid`,
solo esperas explícitas, datos propios creados por API antes de cada prueba y evidencias por caso.

## Estructura

```
src/test/java/com/trouvels360/web/
├── soporte/   Config, FabricaNavegador (Chrome/Firefox/Edge, headless), Api (prepara datos), Datos,
│              Evidencias (extensión JUnit: captura + Allure + reporte), ReporteHtml
├── paginas/   BasePage (esperas y utilidades) + 17 Page Objects (Login, Registro, Buscador, Resultados,
│              HotelDetalle, PagoHotel, PagoDialog, TourDetalle, PagoTour, MisReservas, Perfil, ResenasSeccion,
│              PanelProveedor, ReservasRecibidas, HotelForm, TourForm, ProgramaTour, Navegacion)
└── pruebas/   AutenticacionTest (001–007), HotelesTest (008–012), AprobacionTest (013–014), ToursTest (015–017),
               MisReservasTest (018–020), ResenasItinerarioTest (021–022), ProveedorTest (023–030),
               CoberturaAdicionalTest (031–033: volver conserva la búsqueda, vacío vs. error, sesión vencida)
```

Cada prueba lleva `@Tag("HU-V07")` (sus historias) y `@Tag("TC-WEB-0xx")` (caso), así se puede ejecutar por HU o por caso
y el reporte muestra la trazabilidad.

## Requisitos

- **JDK 21** (o 17), **Maven 3.9+** y Google Chrome (Selenium Manager descarga el driver correcto). Nota sobre Java 25
  en [`qa/README.md`](../README.md#requisitos).
- Las dos APIs (`backend\levantar-todo.bat`) con datos limpios (`backend\api-laravel\reiniciar-datos.bat`); la API
  Python solo la usa TC-WEB-022.
- La web en http://localhost:4200 (`frontend\levantar.bat`, o `frontend\desarrollo.bat` en modo desarrollo).
- En `backend/api-laravel/.env`: `LOGIN_RATE_LIMIT=1000` y `REGISTER_RATE_LIMIT=1000` (las pruebas crean cuentas).
- Las pruebas preparan sus datos llamando directo a la API en http://localhost:8000/api (`api.url`).

## Ejecutar

```bash
mvn test                                   # toda la suite, Chrome headless
mvn test -Dheadless=false                  # viendo el navegador
mvn test -Dgroups=HU-V07                   # solo una historia
mvn test -Dgroups=TC-WEB-011               # solo un caso
mvn test -Dtest=ProveedorTest              # una clase
mvn test -Dbase.url=http://localhost:8080 -Dapi.url=http://localhost:8000/api   # otra URL (p. ej. Docker)
mvn test -Dnavegador=firefox
```

En Windows también: `ejecutar.bat` (toda la suite y abre el reporte).

## Reportes y evidencias

| Reporte | Dónde | Contenido |
|---|---|---|
| Propio (HTML) | `target/reporte-selenium/index.html` | Caso, HU, estado, duración, error y capturas de cada navegador |
| Allure | `mvn allure:serve` (o `allure:report` → `target/site/allure-maven-plugin`) | Pasos, capturas adjuntas, épicas/features |
| Surefire | `target/surefire-reports` | XML JUnit para CI/Jira |

La captura se toma al final de **cada** caso (y se marca `FALLA` si falló) antes de cerrar el navegador.

## Decisiones

- **Independencia:** cada caso registra su propio viajero/proveedor y crea hoteles/tours en una ciudad única
  (`WEB-XXXX`) con fechas lejanas aleatorias; no depende del orden ni de corridas anteriores.
- **Sesión:** las pruebas de login/registro usan la UI; el resto entra con la cookie JWT obtenida por API
  (`Navegacion.entrarComo`) para ganar velocidad sin saltarse nada de lo que se prueba.
- **TC-WEB-013** usa dos navegadores a la vez (proveedor y viajero).
- **TC-WEB-021** usa `qa.viajero1` (tiene estadía finalizada en el Hotel QA) y borra antes su reseña previa.

## Último resultado

**33 / 33 OK** en Chromium headless contra la web compilada para producción y servida por nginx (como en Docker), con la
API reorganizada en `backend/`. Reporte y capturas en `qa/evidencias/etapa-5/selenium/`.
