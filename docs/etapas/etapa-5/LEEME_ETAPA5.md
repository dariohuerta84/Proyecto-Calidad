# Trouvels360 — Etapa 5: pruebas automatizadas y trazabilidad

Esta etapa añade regresión automatizada sobre el producto vigente desarrollado en las etapas 2–4 y completa su trazabilidad hacia Jira. Al cruzar casos y criterios aparecieron **22 criterios sin ningún caso**; se agregaron 14 casos (11 automatizados y 3 manuales) y ahora los **119 criterios** de aceptación quedan cubiertos.

## Organización

| Carpeta | Qué es | Tarea Jira |
|---|---|---|
| `qa/karate/` | API con Karate 1.4.1 — 136 escenarios para los **86** casos TC-API | TRV-601 |
| `qa/jmeter/` | Rendimiento y concurrencia con JMeter 5.6.3 — **6** casos TC-PERF + prueba rápida en Python | TRV-602 |
| `qa/selenium/` | Web con Selenium 4 + JUnit 5 + Page Objects — **33** casos TC-WEB | TRV-603 |
| `qa/appium/` | App Android con Appium 2 + JUnit 5 — **32** casos TC-MOB | TRV-604 |
| `qa/trazabilidad/` | Matriz HU → criterio → caso → código → resultado (160 casos, 119/119 criterios), CSV para Jira y guía | TRV-605 |
| `qa/ejecutar-todo.*` | Corre las suites en orden y regenera la matriz | — |
| `qa/evidencias/etapa-5/` | Reportes de Karate, Selenium y concurrencia de esta etapa | — |
| Código de producto | Correcciones incorporadas directamente en Laravel, Angular y Android | — |

Cada suite tiene su `README.md` con requisitos, comandos y reportes.

## Resultado al preparar la etapa

| Suite | Casos | Resultado |
|---|---|---|
| Karate | 86 casos (136 escenarios) | **136/136 OK**, en paralelo (5 hilos, ~19 s), varias corridas seguidas sin limpiar la base |
| Selenium | 33 | **33/33 OK** en Chromium headless contra la web y el backend locales |
| JMeter | 6 | Plan validado (estructura XML); las carreras TC-PERF-003 y 005 se comprobaron con `concurrencia_smoke.py`: **1×201 + 19×409** y **10 cupos exactos de 30 intentos** |
| Appium | 32 | Se verificaron los 184 resource-id contra la app. **No se ejecutó** (no había emulador en preparación) |

La matriz (`qa/trazabilidad/salida/matriz_trazabilidad.html`) muestra el estado registrado de cada caso: los de Karate y
Selenium con el resultado de su corrida, y los de JMeter/Appium como pendientes hasta que se ejecuten en tu PC.
Después de ejecutarlos, `python qa/trazabilidad/recolectar_resultados.py && python qa/trazabilidad/generar_trazabilidad.py` actualiza la matriz y los CSV de Jira.

## Cambios integrados en etapas anteriores

Estas correcciones ya forman parte del código actual; no se deben copiar manualmente sobre otra carpeta.

- **Laravel:** registro con límite por IP configurable mediante `REGISTER_RATE_LIMIT` (predeterminado: 10 por minuto), usando el limiter `throttle:registro`. El ejemplo está en `backend-laravel/.env.example`.
- **Angular:** resultados de tours distinguen carga, error con reintento y búsqueda vacía; la búsqueda se ejecuta una sola vez. Un `401` sin sesión local lleva al login y conserva `returnUrl`.
- **Android:** registro y cambio de contraseña muestran el error de confirmación al escribir y bloquean el envío mientras las contraseñas difieran.
- **FastAPI:** sin cambios en esta etapa.

Para ejecutar suites que crean muchas cuentas, configura solo el `.env` local del entorno de pruebas (nunca producción):

```
LOGIN_RATE_LIMIT=1000
REGISTER_RATE_LIMIT=1000
CACHE_STORE=file
```

Luego ejecuta `php artisan config:clear` en `backend-laravel/`. No agregues ni sobrescribas los `.env` locales en Git.

## Orden recomendado para probar

1. Levanta Laravel con `backend-laravel/levantar.bat`; este comando recrea la base de datos con `migrate:fresh --seed` e incluye `QaSeeder`.
2. Desde `backend-itinerarios/`, ejecuta `levantar.bat`; desde `frontend-angular/`, ejecuta `iniciar-docker.bat`.
3. Ejecuta `qa/ejecutar-todo.bat` para Karate y Selenium, y para regenerar trazabilidad. Para sumar JMeter y Appium usa `qa/ejecutar-todo.bat jmeter appium`.
4. JMeter requiere JMeter 5.6.3; antes puedes ejecutar `python qa/jmeter/concurrencia_smoke.py`.
5. Appium requiere emulador, servidor Appium y un APK compilado en `android/`. Empieza con `mvn test -Dtest=CuentaMovilTest` desde `qa/appium/`.
6. Para regenerar los archivos de trazabilidad manualmente desde la raíz, ejecuta `python qa/trazabilidad/recolectar_resultados.py` y luego `python qa/trazabilidad/generar_trazabilidad.py`. La guía de Jira está en `qa/trazabilidad/GUIA_JIRA.md`.

## Límites honestos

- **JMeter** no se ejecutó en la preparación: el `.jmx` se validó como XML y su lógica de carreras se comprobó con el script de Python, pero la primera corrida real de JMeter queda pendiente.
- **Appium** no se pudo ejecutar sin emulador: si algún selector o espera necesita ajuste en la primera corrida, basta
  con enviarme el reporte o el error.
- En el entorno de preparación, Karate y Selenium corrieron completos, aunque sus `pom.xml` no se validaron con Maven.
- En este workspace, `npm ci` y `npm run build:prod` de Angular finalizaron correctamente. Quedó una advertencia NG8102 en el template de detalle de hoteles, fuera de los archivos de Etapa 5.
- Android no pudo compilarse aquí: tanto Java global `25.0.4.1` como el JBR de Android Studio `25.0.2` fallan en el parser de Kotlin/Gradle. No se modificó la configuración local de Java.
