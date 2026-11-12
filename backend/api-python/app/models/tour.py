from sqlalchemy import Column, BigInteger, String, Enum, SmallInteger, Integer, DECIMAL, TIMESTAMP, Date, ForeignKey
from sqlalchemy.orm import relationship
from app.config.database import Base


class Tour(Base):
    """Refleja la tabla `tours` de Laravel (solo columnas existentes)."""
    __tablename__ = "tours"

    servicio_id = Column(BigInteger, ForeignKey('servicios.id'), primary_key=True)
    categoria = Column(Enum('Gastronomía', 'Aventura', 'Cultura', 'Relajación'), nullable=False)
    duracion = Column(SmallInteger, nullable=True)       # minutos
    precio = Column(DECIMAL(10, 2), nullable=False)      # por persona
    created_at = Column(TIMESTAMP, nullable=True)
    updated_at = Column(TIMESTAMP, nullable=True)
    deleted_at = Column(TIMESTAMP, nullable=True)

    servicio = relationship("Servicio", back_populates="tour")


class TourActividad(Base):
    __tablename__ = "tour_actividades"

    id = Column(BigInteger, primary_key=True, index=True)
    servicio_id = Column(BigInteger, ForeignKey('servicios.id'), nullable=False, index=True)
    titulo = Column(String(150), nullable=False)
    descripcion = Column(String, nullable=True)
    orden = Column(SmallInteger, nullable=False, default=1)
    duracion_min = Column(SmallInteger, nullable=True)
    direccion = Column(String(255), nullable=True)
    imagen_url = Column(String(500), nullable=True)
    created_at = Column(TIMESTAMP, nullable=True)
    updated_at = Column(TIMESTAMP, nullable=True)
    deleted_at = Column(TIMESTAMP, nullable=True)

    servicio = relationship("Servicio", back_populates="tour_actividades")


class TourSalida(Base):
    """Refleja la tabla `tour_salidas` de Laravel: fechas en que un tour realmente sale."""
    __tablename__ = "tour_salidas"

    id = Column(BigInteger, primary_key=True, index=True)
    servicio_id = Column(BigInteger, ForeignKey('servicios.id'), nullable=False, index=True)
    fecha = Column(Date, nullable=False)
    hora = Column(String(8), nullable=False)
    cupo_total = Column(Integer, nullable=False)
    cupo_reservado = Column(Integer, nullable=False, default=0)
    estado = Column(Enum('programada', 'cerrada', 'cancelada'), nullable=False, default='programada')
    created_at = Column(TIMESTAMP, nullable=True)
    updated_at = Column(TIMESTAMP, nullable=True)
    deleted_at = Column(TIMESTAMP, nullable=True)
