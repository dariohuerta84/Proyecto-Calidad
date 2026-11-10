#!/usr/bin/env bash
set -e

# ==============================================================================
# Script de Inicio y Verificación del SUT (Trouvels360)
# Asignatura: Calidad de Software (C8305 - UPCH)
# ==============================================================================

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "=========================================================="
echo " [SUT Orchestrator] Verificando estado de Trouvels360"
echo "=========================================================="

# 1. Configurar .env en backend-laravel si no existe
if [ ! -f "$ROOT_DIR/backend-laravel/.env" ]; then
    echo "⚙️ Configurando .env para backend-laravel..."
    cat << 'EOF' > "$ROOT_DIR/backend-laravel/.env"
APP_NAME=Trouvels360
APP_ENV=local
APP_KEY=base64:7j8WbH3u8R9vQ2xP1yZ4aB7cD0eF3gH6jK9mN2pQ5rS=
APP_DEBUG=true
APP_URL=http://localhost:8000
APP_TIMEZONE=UTC

DB_CONNECTION=mysql
DB_HOST=mysql
DB_PORT=3306
DB_DATABASE=trouvels360
DB_USERNAME=trouvels_user
DB_PASSWORD=trouvels_pass
DB_ROOT_PASSWORD=trouvels_root

COOKIE_SAMESITE=Lax
COOKIE_SECURE=false

JWT_SECRET=trouvels360_jwt_secret_key_calidad_upch_2026_super_secure!
JWT_TTL=60
JWT_REFRESH_TTL=20160

JWT_MS_SECRET=trouvels360_shared_secret_microservice_fastapi_2026!
JWT_MS_TTL=20
PYTHON_MICROSERVICE_URL=http://trouvels360_itinerarios:8001
EOF
fi

# 2. Configurar .env en backend-itinerarios si no existe
if [ ! -f "$ROOT_DIR/backend-itinerarios/.env.fastapi" ]; then
    echo "⚙️ Configurando .env.fastapi para backend-itinerarios..."
    cat << 'EOF' > "$ROOT_DIR/backend-itinerarios/.env.fastapi"
DB_HOST=mysql
DB_PORT=3306
DB_NAME=trouvels360
DB_USER=trouvels_user
DB_PASSWORD=trouvels_pass

SERVER_HOST=0.0.0.0
SERVER_PORT=8001
SERVER_RELOAD=True

ALLOWED_ORIGINS=*
APP_NAME=Itinerarios Service
APP_VERSION=1.0.0
DEBUG=True

JWT_SECRET=trouvels360_shared_secret_microservice_fastapi_2026!
JWT_ALGORITHM=HS256
JWT_ISSUER=http://localhost:8000
JWT_AUDIENCE=fastapi-itinerarios

HORAS_DISPONIBLES_POR_DIA=8
EOF
fi

echo "✅ Archivos de entorno configurados."
echo "💡 Para iniciar los contenedores de desarrollo ejecuta:"
echo "   docker compose up -d --build"
echo "=========================================================="
