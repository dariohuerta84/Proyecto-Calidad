# API Trouvels360 (Laravel 12 + Apache) — una sola imagen para local y para la nube.
FROM php:8.2-apache

# Extensiones de PHP que usa Laravel y MySQL
RUN apt-get update && apt-get install -y --no-install-recommends \
        git unzip libzip-dev libpng-dev libonig-dev libxml2-dev curl \
    && docker-php-ext-install pdo_mysql mbstring exif pcntl bcmath gd zip \
    && a2enmod rewrite headers \
    && echo "ServerName localhost" >> /etc/apache2/apache2.conf \
    && rm -rf /var/lib/apt/lists/*

COPY --from=composer:2 /usr/bin/composer /usr/bin/composer

WORKDIR /var/www/html

# 1) Dependencias primero (se cachean mientras composer.json/lock no cambien).
#    Se incluyen las de desarrollo para poder correr las pruebas Pest dentro del contenedor.
COPY composer.json composer.lock ./
RUN composer install --no-interaction --no-progress --prefer-dist --no-scripts --no-autoloader

# 2) Código de la aplicación
COPY . .
RUN composer dump-autoload --optimize \
    && mkdir -p storage/logs storage/framework/cache storage/framework/sessions storage/framework/views bootstrap/cache \
    && chown -R www-data:www-data storage bootstrap/cache \
    && chmod -R 775 storage bootstrap/cache

# 3) Apache sirve /public; el puerto se toma de PORT al iniciar (80 por defecto, la nube puede cambiarlo)
COPY docker/apache/000-default.conf /etc/apache2/sites-available/000-default.conf
COPY docker/entrypoint.sh /usr/local/bin/entrypoint.sh
RUN sed -i 's/\r$//' /usr/local/bin/entrypoint.sh && chmod +x /usr/local/bin/entrypoint.sh

EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fs "http://localhost:${PORT:-80}/api/ping" || exit 1

ENTRYPOINT ["entrypoint.sh"]
CMD ["apache2-foreground"]
