# Trouvels360 — Etapa 4: App móvil Android y corrección web

Este hito añade Android y corrige referencias de historias de usuario en la web. El código vigente está en la raíz; esta carpeta conserva solo el registro de la etapa.

| Carpeta | Qué es |
|---|---|
| `../../../android/` | App Kotlin + Jetpack Compose para **viajero y proveedor** (todas las HU marcadas como Móvil) |
| `../../../frontend-angular/` | Web con la corrección de trazabilidad integrada (solo comentarios y documentos) |

El backend Laravel y el microservicio Python **no cambian** respecto de la etapa 3; la única copia vigente está en la raíz.

## Cómo probarla

1. Levanta el backend y el microservicio de itinerarios siguiendo las [instrucciones de Etapa 3](../etapa-3/LEEME_ETAPA3.md).
2. Abre `../../../android/` en Android Studio, deja que Gradle sincronice y ejecuta ▶ en un emulador (API 26+).
3. Viajero `edwinviajero@gmail.com` / `viajero` · Proveedor `edwinproveedor@gmail.com` / `proveedor` ·
   tarjeta `4242 4242 4242 4242`, `12/30`, CVC `123`.

## Documentos

- `../../../android/README.md` — pantallas, HU y endpoints de cada una.
- `../../../android/docs/CAMBIOS_ETAPA4.md` — qué cambió y cómo se verificó.
- `../../../android/docs/CONTRATO_API.md` — los endpoints que usa la app y cómo se validaron.
- `../../../android/docs/SELECTORES_APPIUM.md` y `../../../android/docs/APPIUM.md` — ids y flujo para la etapa 5.

Importante: no se pudo generar el APK en el entorno de preparación (sin SDK de Android). Si Android Studio
muestra algún error al compilar, copia el mensaje y se corrige.
