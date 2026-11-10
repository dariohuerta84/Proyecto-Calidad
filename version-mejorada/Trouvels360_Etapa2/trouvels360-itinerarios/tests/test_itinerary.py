"""TRV-404 — microservicio de itinerarios."""
from tests.conftest import token


def test_health(client):
    assert client.get("/api/health").json()["status"] == "healthy"


def test_sin_token_responde_401(client, cuerpo):
    r = client.post("/api/itinerary", json=cuerpo)
    assert r.status_code == 401


def test_token_con_firma_o_audiencia_incorrecta_responde_401(client, cuerpo):
    assert client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token(aud="otra")}).status_code == 401
    malo = token()[:-4] + "abcd"
    assert client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + malo}).status_code == 401


def test_token_expirado_responde_401(client, cuerpo):
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token(exp=1)})
    assert r.status_code == 401


def test_itinerario_valido(client, cuerpo):
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    assert r.status_code == 200, r.text
    data = r.json()["data"]
    assert data["hotel"]["id"] == 1
    assert data["resumen"]["total_dias"] == 2
    nombres = [a["nombre"] for d in data["itinerario"] for a in d["actividades"]]
    assert "Tour Eliminado" not in nombres
    for dia in data["itinerario"]:
        assert dia["horas_totales"] <= 8
        for act in dia["actividades"]:
            assert act["duracion_horas"] > 0 and act["distancia_km"] >= 0


def test_interes_invalido_responde_422(client, cuerpo):
    cuerpo["interes"] = "Buceo"
    assert client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()}).status_code == 422


def test_mas_de_14_dias_responde_422(client, cuerpo):
    from datetime import date, timedelta
    cuerpo["fecha_checkout"] = str(date.fromisoformat(cuerpo["fecha_checkin"]) + timedelta(days=15))
    assert client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()}).status_code == 422


def test_sin_actividades_para_el_interes_responde_404(client, cuerpo):
    cuerpo["interes"] = "Gastronomía"
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    assert r.status_code == 404
    assert "No hay actividades" in r.json()["detail"]["error"]


def test_hotel_de_otra_ciudad_responde_404(client, cuerpo):
    cuerpo["destino"] = "Lima"
    assert client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()}).status_code == 404
