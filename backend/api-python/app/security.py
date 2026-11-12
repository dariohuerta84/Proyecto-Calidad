# app/security.py
"""
Autenticación servicio-a-servicio (HU-V15).

Laravel firma un JWT HS256 con la clave compartida JWT_SECRET (= JWT_MS_SECRET en Laravel),
audiencia 'fastapi-itinerarios', emisor = APP_URL de Laravel y expiración corta.
Toda petición a /api/itinerary debe traer ese token: sin token o con token inválido → 401.
"""
import logging
from typing import Optional

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from jose import ExpiredSignatureError, JWTError, jwt
from pydantic import BaseModel

from app.config.settings import get_settings

logger = logging.getLogger(__name__)
bearer_scheme = HTTPBearer(auto_error=False)


class TokenData(BaseModel):
    sub: int
    email: Optional[str] = None
    rol: Optional[str] = None


def _no_autorizado(detalle: str) -> HTTPException:
    return HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail={"success": False, "error": detalle},
        headers={"WWW-Authenticate": "Bearer"},
    )


def get_current_user(token: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme)) -> TokenData:
    if token is None or not token.credentials:
        raise _no_autorizado("Falta el token de servicio.")

    settings = get_settings()
    try:
        payload = jwt.decode(
            token.credentials,
            settings.JWT_SECRET,
            algorithms=[settings.JWT_ALGORITHM],
            audience=settings.JWT_AUDIENCE,
            issuer=settings.JWT_ISSUER,
        )
    except ExpiredSignatureError:
        raise _no_autorizado("El token de servicio expiró.")
    except JWTError as e:
        # Nunca registrar la clave ni el token.
        logger.warning("JWT de servicio rechazado: %s", type(e).__name__)
        raise _no_autorizado("Token de servicio inválido.")

    if payload.get("sub") is None:
        raise _no_autorizado("Token de servicio sin usuario.")

    return TokenData(sub=int(payload["sub"]), email=payload.get("email"), rol=payload.get("rol"))
