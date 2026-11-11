# Trouvels360 Backend – Guía Docker (Paso a Paso Definitivo)

---
## 1. Prerrequisitos
Instala Docker Desktop (Windows / Mac) o Docker Engine (Linux). Descarga: https://docker.com/products/docker-desktop

Verifica instalación:
```bash
docker --version
docker compose version  # opcional
```

---
## 2. Primera vez (instalación inicial)

### Windows (PowerShell)
```powershell

docker-compose up --build -d

docker-compose exec app composer install

bash scripts/dev-init.sh --fresh             
```

### Linux / macOS / Git Bash / WSL
```bash
cd /ruta/al/proyecto/trouvels360-backend-laravel

docker-compose up --build -d

docker-compose exec app composer install

bash scripts/dev-init.sh --fresh      
```

Verifica acceso:
- API: http://localhost:8000/api/ping
- phpMyAdmin: http://localhost:8080

Si todo responde → listo para desarrollar.

---
## 3. Flujo diario de desarrollo
```bash
docker-compose up -d        # Arrancar (si ya está todo configurado)
# ... trabajar ...
docker-compose down         # Detener todo
```
Ver logs en otra terminal:
```bash
docker-compose logs -f app
```

---
## 4. Después de crear o modificar migraciones
Generaste una migración nueva o editaste una existente:
```bash
docker-compose exec app php artisan migrate
```
Si rompiste algo y necesitas rehacer rápido el esquema (sin preservar datos):
```bash
docker-compose exec app php artisan migrate:fresh --seed
```

---
## 5. Reset completo (BORRA TODOS LOS DATOS)
Úsalo cuando quieras limpiar completamente la base (ej: refactor grande).
```bash
docker-compose down -v --rmi local --remove-orphans   # Elimina contenedores, volumen y la imagen local de app
docker-compose up -d                                   # Recrea servicios
bash scripts/dev-init.sh --fresh                       # migrate:fresh + seed
```
Sin seeds:
```bash
bash scripts/dev-init.sh    # solo migraciones (genera APP_KEY si falta)
```

---
## 6. Estructura relevante
```
trouvels360-backend-laravel/
├── docker-compose.yml           # Orquestación
├── Dockerfile                   # Imagen PHP 8.2 + Apache (desarrollo)
├── .env                         # Variables de entorno (copia de .env.example)
├── scripts/
│   └── dev-init.sh              # Helper para inicialización
└── docker/
    └── apache/
        └── 000-default.conf     # Configuración de VirtualHost Apache
```

---
## 7. Errores comunes

| Problema | Causa típica | Solución |
|----------|--------------|----------|
| `Connection refused` a MySQL | Migraciones lanzadas antes de que MySQL esté healthy | Espera unos segundos o revisa `docker-compose ps` / `logs mysql` |
| APP responde 500 al inicio | Falta `APP_KEY` | Ejecuta `bash scripts/dev-init.sh` |
| Cambié `.env` y no toma efecto | Config cache en memoria | `docker-compose exec app php artisan config:clear` |
| phpMyAdmin pide credenciales y falla | Variables DB incorrectas | Verifica `.env` DB_USERNAME / DB_PASSWORD y reinicia contenedor `mysql` |
| Migración falla por tabla existente | Editaste migraciones viejas | Usa `migrate:fresh` si estás en desarrollo |
| Puerto 8000 ocupado | Otro servicio lo usa | Edita `docker-compose.yml` (ej: `8081:80`) y actualiza `APP_URL` |
| Composer tarda mucho | Cache fría | Subsecuentes builds serán más rápidos gracias a capas Docker |

Ver logs rápido:
```bash
docker-compose logs --tail=100 app
docker-compose logs --tail=100 mysql
```

### Cambiar variables
Cambiaste algo en `.env` (no relacionado con dependencias del contenedor):
```bash
docker-compose exec app php artisan config:clear
```
Cambiaste dependencias de sistema (Dockerfile) → reconstruir:
```bash
docker-compose build --no-cache app
docker-compose up -d
```
