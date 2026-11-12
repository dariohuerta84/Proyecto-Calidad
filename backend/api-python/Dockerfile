# API de itinerarios de Trouvels360 (FastAPI) — una sola imagen para local y para la nube.
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

# Dependencias primero (se cachean mientras requirements no cambien). pymysql es Python puro: no hace falta compilar nada.
COPY requirements.txt requirements-dev.txt ./
RUN pip install -r requirements-dev.txt

COPY app ./app
COPY tests ./tests

# Usuario sin privilegios
RUN useradd --create-home api && chown -R api /app
USER api

# PORT lo define la nube (Railway, Render...); en local es 8001
ENV PORT=8001
EXPOSE 8001
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=5 \
    CMD python -c "import os, urllib.request; urllib.request.urlopen(f'http://localhost:{os.environ.get(\"PORT\", \"8001\")}/api/health')"

CMD ["sh", "-c", "uvicorn app.main:app --host 0.0.0.0 --port ${PORT} --proxy-headers --forwarded-allow-ips='*'"]
