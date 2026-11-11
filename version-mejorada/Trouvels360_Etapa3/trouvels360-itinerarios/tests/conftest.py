"""Pruebas del microservicio con SQLite en memoria (sin MySQL)."""
import os
import time
from datetime import date, timedelta

os.environ.setdefault("JWT_SECRET", "secreto-de-prueba-123")
os.environ.setdefault("JWT_ISSUER", "http://localhost:8000")

import pytest
from fastapi.testclient import TestClient
from jose import jwt
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool

from app.config.database import Base, get_db
from app.main import app
from app.models.review import Review
from app.models.servicio import Hotel, Servicio
from app.models.tour import Tour, TourActividad, TourSalida

engine = create_engine("sqlite://", connect_args={"check_same_thread": False}, poolclass=StaticPool)
TestingSession = sessionmaker(bind=engine, autoflush=False, autocommit=False)


def _override_db():
    db = TestingSession()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = _override_db


@pytest.fixture(autouse=True)
def datos():
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)
    db = TestingSession()
    db.add(Servicio(id=1, proveedor_id=1, nombre="Hotel Centro", tipo="hotel", ciudad="Cusco", pais="Perú",
                    latitud=-13.5163, longitud=-71.9785, activo=True))
    db.add(Hotel(servicio_id=1, direccion="Av. Sol 100", estrellas=4))
    tours = [
        (10, "Valle Sagrado", -13.33, -72.08, 120, 5),
        (11, "Rainbow Mountain", -13.87, -71.30, 150, 4),
        (12, "Caminata Sacsayhuamán", -13.51, -71.98, 60, 5),
    ]
    for sid, nombre, lat, lon, precio, estrellas in tours:
        db.add(Servicio(id=sid, proveedor_id=1, nombre=nombre, tipo="tour", ciudad="Cusco", pais="Perú",
                        latitud=lat, longitud=lon, activo=True))
        db.add(Tour(servicio_id=sid, categoria="Aventura", duracion=240, precio=precio))
        db.add(TourActividad(id=sid, servicio_id=sid, titulo="Actividad", orden=1, duracion_min=180))
        db.add(Review(id=sid, servicio_id=sid, usuario_id=2, comentario="Muy bueno", calificacion=estrellas))
    # Salidas (estadía de prueba: día +5 y +6). Valle Sagrado sale el día +5; Rainbow el +6;
    # Sacsayhuamán solo tiene una salida LLENA (+5), una CERRADA (+6) y una fuera de rango (+20).
    hoy = date.today()
    d = lambda n: hoy + timedelta(days=n)
    db.add_all([
        TourSalida(id=1, servicio_id=10, fecha=d(5), hora="08:00", cupo_total=10, cupo_reservado=2, estado="programada"),
        TourSalida(id=2, servicio_id=11, fecha=d(6), hora="05:00", cupo_total=10, cupo_reservado=0, estado="programada"),
        TourSalida(id=3, servicio_id=12, fecha=d(5), hora="09:00", cupo_total=5, cupo_reservado=5, estado="programada"),
        TourSalida(id=4, servicio_id=12, fecha=d(6), hora="09:00", cupo_total=5, cupo_reservado=0, estado="cerrada"),
        TourSalida(id=5, servicio_id=12, fecha=d(20), hora="09:00", cupo_total=5, cupo_reservado=0, estado="programada"),
    ])
    # Tour de día completo (12 h) con salida el día +6
    db.add(Servicio(id=14, proveedor_id=1, nombre="Montaña Full Day", tipo="tour", ciudad="Cusco", pais="Perú",
                    latitud=-13.86, longitud=-71.30, activo=True))
    db.add(Tour(servicio_id=14, categoria="Cultura", duracion=720, precio=90))
    db.add(TourSalida(id=6, servicio_id=14, fecha=d(6), hora="04:00", cupo_total=10, cupo_reservado=0, estado="programada"))
    # Tour eliminado lógicamente: no debe aparecer
    from datetime import datetime
    db.add(Servicio(id=13, proveedor_id=1, nombre="Tour Eliminado", tipo="tour", ciudad="Cusco", pais="Perú",
                    latitud=-13.5, longitud=-71.9, activo=True, deleted_at=datetime.now()))
    db.add(Tour(servicio_id=13, categoria="Aventura", duracion=60, precio=10))
    db.commit()
    db.close()
    yield


def token(**extra):
    now = int(time.time())
    claims = {"sub": "5", "email": "qa@trouvels360.test", "rol": "viajero", "iss": "http://localhost:8000",
              "aud": "fastapi-itinerarios", "iat": now, "exp": now + 300}
    claims.update(extra)
    return jwt.encode(claims, os.environ["JWT_SECRET"], algorithm="HS256")


@pytest.fixture
def client():
    return TestClient(app)


@pytest.fixture
def cuerpo():
    hoy = date.today()
    return {"destino": "Cusco", "hotel_id": 1, "fecha_checkin": str(hoy + timedelta(days=5)),
            "fecha_checkout": str(hoy + timedelta(days=7)), "interes": "Aventura"}
