# Web Trouvels360 (Angular 19) — una sola imagen para local y para la nube.
# Etapa 1: compila la web. Etapa 2: nginx sirve los archivos y reenvía /api a la API Laravel.

FROM node:20-alpine AS compilacion
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npx ng build --configuration production

FROM nginx:1.27-alpine
# Valores por defecto; se cambian al iniciar el contenedor (variables de entorno), sin recompilar
ENV API_URL=http://host.docker.internal:8000 \
    PORT=80
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=compilacion /app/dist/trouvels360-web/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=5 CMD wget -qO- "http://127.0.0.1:${PORT}/salud" >/dev/null || exit 1
