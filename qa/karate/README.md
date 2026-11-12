# Pruebas de API — Karate 1.4.1 (TRV-601)

**136 escenarios** que cubren los **86 casos TC-API-001 a TC-API-086** de las 24 historias (78 definidos en la etapa 1 y 8 agregados en la etapa 5 para los criterios que no tenían caso). Cada escenario lleva la etiqueta
de su historia y de su caso (`@TRV-203 @TC-API-026`), valida código, esquema JSON, reglas de negocio y tiempo de respuesta
(`responseTime < 1000 ms`; 2000 ms con `karate.env=ci`).

## Estructura

```
src/test/java/
├── karate-config.js            URL base, umbral de tiempo, utilidades (correo único, RUC, fechas libres, tarjeta de prueba),
│                               como(sesión) / anonimo() y las sesiones compartidas (callSingle: un login por usuario)
└── trouvels360/
    ├── TrouvelsApiTest.java    Runner JUnit 5 en paralelo (reportes HTML, JUnit XML y Cucumber JSON)
    ├── auth.feature            TRV-101..103  TC-API-001..012   registro, login, refresh, logout, CSRF, límites
    ├── cuenta.feature          TRV-104..105  TC-API-013..019   perfil, contraseña, eliminar cuenta
    ├── hoteles.feature         TRV-201..202  TC-API-020..025   búsqueda, filtros, disponibilidad, cotización (CSV)
    ├── reservas-hotel.feature  TRV-203       TC-API-026..030   pago simulado, tarjetas inválidas (CSV), stock, idempotencia
    ├── aprobacion.feature      TRV-204       TC-API-031..035   aceptar/rechazar, 409, 403, motivo obligatorio
    ├── tours.feature           TRV-301..302  TC-API-036..040   filtros (CSV), detalle, cotización (CSV), cupo
    ├── reservas-tour.feature   TRV-303       TC-API-041..043   confirmación automática, salidas cerradas/pasadas, idempotencia
    ├── mis-reservas.feature    TRV-401..402  TC-API-044..049   listado paginado, estados, ajena 404, cancelación y liberación
    ├── resenas.feature         TRV-403       TC-API-050..053   resumen, elegibilidad, validaciones (CSV en línea), autoría
    ├── itinerario.feature      TRV-404       TC-API-054..055   microservicio Python vía Laravel
    ├── proveedor.feature       TRV-501..508  TC-API-056..078   panel, hoteles, habitaciones, tours, actividades, salidas,
    │                                                           bandeja, eliminación
    ├── cobertura-adicional.feature        TC-API-079..086   recotizar, tour sin salidas, pago de tour inválido, salida
    │                                                           cancelada, editar/transacción de hotel, tarifa 0, doble eliminación
    ├── helpers/  (@ignore)     sesión, CSRF, registrar, crear hotel/tour propio, reservar, fixtures del QaSeeder
    ├── schemas/                11 esquemas JSON (usuario, meta, hotel, habitación, reservas, cotización, reseña, salida...)
    └── data/                   CSV de los casos data-driven
```

## Independencia de los datos

Cada escenario crea lo que necesita (hoteles y tours en ciudades `QA-<único>`, fechas lejanas aleatorias, viajeros o
proveedores nuevos cuando cuenta conteos), así que la suite corre **en paralelo** y se puede repetir sin limpiar la base.
`resenas.feature` es `@parallel=false` porque usa a `qa.viajero1`, el único con estadías finalizadas (QaSeeder).

## Requisitos

1. Backend levantado con `php artisan migrate:fresh --seed` (incluye el `QaSeeder`).
2. Microservicio de itinerarios en marcha (solo `itinerario.feature`).
3. En el `.env` del backend de pruebas: `LOGIN_RATE_LIMIT=1000`, `REGISTER_RATE_LIMIT=1000` y `CACHE_STORE=file`
   (la lista negra de JWT del logout necesita una caché persistente).

## Ejecutar

```bash
mvn test                                          # todo, 4 hilos
mvn test -Dhilos=8
mvn test -Dkarate.env=ci                          # usa la variable de entorno API_URL y umbral de 2 s
mvn test "-Dkarate.options=--tags @TRV-203"       # una historia
mvn test "-Dkarate.options=--tags @TC-API-029"    # un caso
```

Windows: `ejecutar.bat`. Reporte: `target/karate-reports/karate-summary.html` (más `*.xml` JUnit y `*.json` Cucumber
para importar en Jira/Xray).

## Resultado al preparar la etapa

`136 escenarios · 136 OK · 0 fallas` en paralelo con 5 hilos (~19 s); la suite se corrió varias veces seguidas sin limpiar la base. Los reportes de esa corrida se conservan en `qa/evidencias/etapa-5/karate/`.
