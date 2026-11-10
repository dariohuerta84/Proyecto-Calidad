#!/bin/bash
# Inicialización de entorno de desarrollo para Trouvels360
set -e

echo "[dev-init] Inicio"

if [ ! -f .env ]; then
  echo "[dev-init] ERROR: No existe .env. Copia primero .env.docker.example a .env"
  exit 1
fi

if ! grep -q '^APP_KEY=' .env || grep -q '^APP_KEY=$' .env; then
  echo "[dev-init] Generando APP_KEY..."
  docker-compose exec app php artisan key:generate
fi

echo "[dev-init] Ejecutando migraciones..."
docker-compose exec app php artisan migrate

if [ "$1" = "--seed" ]; then
  echo "[dev-init] Ejecutando seeders..."
  docker-compose exec app php artisan db:seed
fi

if [ "$1" = "--fresh" ]; then
  echo "[dev-init] Modo --fresh (migrate:fresh --seed)"
  docker-compose exec app php artisan migrate:fresh --seed
fi

echo "[dev-init] Cache limpias (opcional)"
docker-compose exec app php artisan config:clear || true

echo "[dev-init] Listo."