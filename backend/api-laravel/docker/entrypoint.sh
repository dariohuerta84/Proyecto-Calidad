#!/bin/sh
# Arranque del contenedor de la API Laravel.
#  1. Ajusta el puerto de Apache (variable PORT, 80 por defecto).
#  2. Espera a que la base de datos acepte conexiones.
#  3. Prepara la base según BD_AL_INICIAR:
#       migrar    (por defecto) aplica migraciones pendientes y carga los datos de prueba solo si la base está vacía
#       reiniciar borra todo y vuelve a crear la base con los datos de prueba (migrate:fresh --seed)
#       nada      no toca la base
#  4. Inicia Apache.
set -e
cd /var/www/html

PUERTO="${PORT:-80}"
sed -ri "s/^Listen [0-9]+/Listen ${PUERTO}/" /etc/apache2/ports.conf
sed -ri "s/<VirtualHost \*:[0-9]+>/<VirtualHost *:${PUERTO}>/" /etc/apache2/sites-available/000-default.conf

if [ -z "$APP_KEY" ] || [ -z "$JWT_SECRET" ]; then
    echo "[api-laravel] ERROR: faltan APP_KEY o JWT_SECRET. Copia .env.example como .env (trae valores para desarrollo local)."
    exit 1
fi

if [ "${DB_CONNECTION:-mysql}" = "mysql" ]; then
    echo "[api-laravel] Esperando a MySQL en ${DB_HOST}:${DB_PORT:-3306}..."
    i=0
    until php -r 'try { new PDO("mysql:host=".getenv("DB_HOST").";port=".(getenv("DB_PORT") ?: 3306).";dbname=".getenv("DB_DATABASE"), getenv("DB_USERNAME"), getenv("DB_PASSWORD")); exit(0); } catch (Throwable $e) { exit(1); }'; do
        i=$((i + 1))
        if [ "$i" -ge 60 ]; then
            echo "[api-laravel] ERROR: MySQL no respondió en 2 minutos. Revisa DB_HOST, DB_USERNAME y DB_PASSWORD."
            exit 1
        fi
        sleep 2
    done
fi

php artisan config:clear >/dev/null

case "${BD_AL_INICIAR:-migrar}" in
    reiniciar)
        echo "[api-laravel] Recreando la base con datos de prueba (migrate:fresh --seed)..."
        php artisan migrate:fresh --seed --force
        ;;
    migrar)
        php artisan migrate --force
        USUARIOS=$(php -r 'require "vendor/autoload.php"; $app = require "bootstrap/app.php"; $app->make(Illuminate\Contracts\Console\Kernel::class)->bootstrap(); echo Illuminate\Support\Facades\DB::table("usuarios")->count();')
        if [ "$USUARIOS" = "0" ]; then
            echo "[api-laravel] Base vacía: cargando datos de prueba..."
            php artisan db:seed --force
        fi
        ;;
    *)
        echo "[api-laravel] BD_AL_INICIAR=${BD_AL_INICIAR}: no se toca la base."
        ;;
esac

chown -R www-data:www-data storage bootstrap/cache
echo "[api-laravel] Lista en el puerto ${PUERTO} (salud: /api/ping)."
exec "$@"
