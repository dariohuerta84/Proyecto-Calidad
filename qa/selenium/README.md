# Pruebas E2E web — Selenium 4 + JUnit 5 (TRV-603)

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

Cada prueba lleva `@Tag("TRV-xxx")` (historia) y `@Tag("TC-WEB-0xx")` (caso), así se puede ejecutar por HU o por caso
y el reporte muestra la trazabilidad.

## Requisitos

- Java 17+, Maven 3.9+, Google Chrome (Selenium Manager descarga el chromedriver correcto).
- Backend en `http://localhost:8000` con `php artisan migrate:fresh --seed` (incluye el **QaSeeder**) y el microservicio
  de itinerarios en `:8001` (solo para TC-WEB-022).
- Web en `http://localhost:4200` (`npm start`).
- En el `.env` del backend de pruebas: `LOGIN_RATE_LIMIT=1000` y `REGISTER_RATE_LIMIT=1000` (las pruebas crean cuentas).

## Ejecutar

```bash
mvn test                                   # toda la suite, Chrome headless
mvn test -Dheadless=false                  # viendo el navegador
mvn test -Dgroups=TRV-203                  # solo una historia
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

## Resultado al preparar la etapa

33/33 casos en verde contra la web y el backend locales (Chromium 153 headless). El detalle de esa corrida está en `qa/evidencias/etapa-5/selenium/`.
