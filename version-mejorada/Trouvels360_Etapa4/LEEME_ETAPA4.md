# Trouvels360 — Etapa 4: app móvil Android

| Carpeta | Qué es |
|---|---|
| `trouvels360-android/` | App Kotlin + Jetpack Compose para **viajero y proveedor** (todas las HU marcadas como Móvil) |
| `correcciones-web-etapa3/` | 6 archivos de la web con los códigos de HU corregidos (solo comentarios y documentos) |

El backend Laravel y el microservicio Python **no cambian**: usa los de la etapa 3.

## Cómo probarla

1. Levanta el backend y el microservicio de itinerarios (`LEEME_ETAPA3.md`).
2. Abre `trouvels360-android` en Android Studio, deja que Gradle sincronice y ejecuta ▶ en un emulador (API 26+).
3. Viajero `edwinviajero@gmail.com` / `viajero` · Proveedor `edwinproveedor@gmail.com` / `proveedor` ·
   tarjeta `4242 4242 4242 4242`, `12/30`, CVC `123`.

## Documentos

- `trouvels360-android/README.md` — pantallas, HU y endpoints de cada una.
- `trouvels360-android/docs/CAMBIOS_ETAPA4.md` — qué cambió y cómo se verificó.
- `trouvels360-android/docs/CONTRATO_API.md` — los 52 endpoints que usa la app y cómo se validaron.
- `trouvels360-android/docs/SELECTORES_APPIUM.md` y `docs/APPIUM.md` — ids y flujo para la etapa 5.

Importante: no se pudo generar el APK en el entorno de preparación (sin SDK de Android). Si Android Studio
muestra algún error al compilar, copia el mensaje y se corrige.
