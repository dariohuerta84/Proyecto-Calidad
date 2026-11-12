# Pruebas de API — Karate 1.5.2 (QA-01)

**136 escenarios** que cubren los **86 casos TC-API-001 a TC-API-086**. Cada escenario lleva la etiqueta de sus
historias y de su caso (`@HU-V07 @TC-API-026`) y valida código HTTP, esquema JSON, reglas de negocio y tiempo de
respuesta (`responseTime < 1000 ms`; 2000 ms con `karate.env=ci`).

> **Versión:** la suite pasó 136/136 con Karate 1.4.1, pero esa versión ya no se descarga de Maven Central
> (`Could not find artifact io.karatelabs:karate-junit5:jar:1.4.1`), así que el `pom.xml` usa 1.5.2.
> Si Maven sigue sin encontrarla, corre una vez `mvn -U test` para que vuelva a consultar el repositorio.

## Estructura

```
src/test/java/
├── karate-config.js            URL base, umbral de tiempo, utilidades (correo único, RUC, fechas libres, tarjeta de prueba),
│                               como(sesión) / anonimo() y las sesiones compartidas (callSingle: un login por usuario)
└── trouvels360/
    ├── TrouvelsApiTest.java    Runner JUnit 5 en paralelo (reportes HTML, JUnit XML y Cucumber JSON)
    ├── auth.feature                 TC-API-001..012  HU-V01, HU-V02, HU-P01, HU-P02  registro, login, logout, CSRF, roles
    ├── cuenta.feature               TC-API-013..019  HU-V03, HU-V04, HU-P02          perfil, contraseña, eliminar cuenta
    ├── hoteles.feature              TC-API-020..025  HU-V05, HU-V06                  búsqueda, disponibilidad, cotización (CSV)
    ├── reservas-hotel.feature       TC-API-026..030  HU-V07                          pago simulado (CSV), stock, idempotencia
    ├── aprobacion.feature           TC-API-031..035  HU-P13, HU-P14, HU-V12          aceptar, rechazar, 409, 403, motivo
    ├── tours.feature                TC-API-036..040  HU-V08, HU-V09                  filtros (CSV), detalle, cotización, cupo
    ├── reservas-tour.feature        TC-API-041..043  HU-V10, HU-V12                  confirmación automática, salidas no válidas
    ├── mis-reservas.feature         TC-API-044..049  HU-V11, HU-V12, HU-V13          listado paginado, estados, cancelación
    ├── resenas.feature              TC-API-050..053  HU-V14, HU-P04                  resumen, elegibilidad, autoría
    ├── itinerario.feature           TC-API-054..055  HU-V15                          API Python a través de Laravel
    ├── proveedor.feature            TC-API-056..078  HU-P03 a HU-P12, HU-P15         panel, hoteles, habitaciones, tours, salidas…
    ├── cobertura-adicional.feature  TC-API-079..086  varias                          criterios que no tenían caso en la etapa 1
    ├── helpers/  (@ignore)     sesión, CSRF, registrar, crear hotel/tour propio, reservar, fixtures del QaSeeder
    ├── schemas/                11 esquemas JSON (usuario, meta, hotel, habitación, reservas, cotización, reseña, salida...)
    └── data/                   CSV de los casos data-driven
```

## Independencia de los datos

Cada escenario crea lo que necesita (hoteles y tours en ciudades `QA-<único>`, fechas lejanas aleatorias, viajeros o
proveedores nuevos cuando cuenta conteos), así que la suite corre **en paralelo** y se puede repetir sin limpiar la base.
`resenas.feature` es `@parallel=false` porque usa a `qa.viajero1`, el único con estadías finalizadas (QaSeeder).

## Requisitos

1. **JDK 21** (o 17) y **Maven 3.9+** (ver la nota sobre Java 25 en [`qa/README.md`](../README.md#requisitos)).
2. Las dos APIs levantadas (`backend\levantar-todo.bat`) con datos limpios (`backend\api-laravel\reiniciar-datos.bat`).
   La API Python solo la usa `itinerario.feature`.
3. En `backend/api-laravel/.env`: `LOGIN_RATE_LIMIT=1000` y `REGISTER_RATE_LIMIT=1000`.
4. La URL base es `http://localhost:8000/api`; se cambia con la variable de entorno `API_URL`.

## Ejecutar

```bash
mvn test                                          # todo, 4 hilos
mvn test -Dhilos=8
mvn test -Dkarate.env=ci                          # usa la variable de entorno API_URL y umbral de 2 s
mvn test "-Dkarate.options=--tags @HU-V07"       # una historia
mvn test "-Dkarate.options=--tags @TC-API-029"    # un caso
```

Windows: `ejecutar.bat`. Reporte: `target/karate-reports/karate-summary.html` (más `*.xml` JUnit y `*.json` Cucumber
para importar en Jira/Xray).

## Último resultado

`136 escenarios · 136 OK · 0 fallas` en paralelo con 5 hilos (~18 s), contra la API reorganizada en `backend/`. Los
reportes de esa corrida están en `qa/evidencias/karate/`.
