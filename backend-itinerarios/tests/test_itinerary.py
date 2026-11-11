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
        # Máximo 8 h por día, salvo un único tour de día completo
        assert dia["horas_totales"] <= 8 or len(dia["actividades"]) == 1
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


def test_solo_propone_tours_en_fechas_con_salida_y_cupo(client, cuerpo):
    from datetime import date, timedelta
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    assert r.status_code == 200, r.text
    dias = r.json()["data"]["itinerario"]
    ci = date.fromisoformat(cuerpo["fecha_checkin"])
    por_dia = {d["dia"]: [(a["nombre"], a["salida"]["salida_id"], a["salida"]["hora"]) for a in d["actividades"]] for d in dias}
    # Valle Sagrado solo sale el día 1 y Rainbow Mountain el día 2
    assert por_dia[1] == [("Valle Sagrado", 1, "08:00")]
    assert por_dia[2] == [("Rainbow Mountain", 2, "05:00")]
    assert dias[0]["fecha_sugerida"] == str(ci)
    # Sacsayhuamán: salida llena, cerrada o fuera de la estadía → no se propone
    nombres = [a["nombre"] for d in dias for a in d["actividades"]]
    assert "Caminata Sacsayhuamán" not in nombres


def test_usa_la_duracion_declarada_del_tour(client, cuerpo):
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    act = r.json()["data"]["itinerario"][0]["actividades"][0]
    assert act["duracion_horas"] == 4.0  # Tour.duracion = 240 min


def test_sin_salidas_en_la_estadia_responde_404(client, cuerpo):
    from datetime import date, timedelta
    ci = date.today() + timedelta(days=30)
    cuerpo["fecha_checkin"], cuerpo["fecha_checkout"] = str(ci), str(ci + timedelta(days=2))
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    assert r.status_code == 404
    assert "con salidas entre" in r.json()["detail"]["error"]


def test_tour_de_dia_completo_ocupa_un_dia_vacio(client, cuerpo):
    cuerpo["interes"] = "Cultura"
    r = client.post("/api/itinerary", json=cuerpo, headers={"Authorization": "Bearer " + token()})
    assert r.status_code == 200, r.text
    dias = r.json()["data"]["itinerario"]
    assert dias[0]["actividades"] == []                       # sin salida el día 1
    assert [a["nombre"] for a in dias[1]["actividades"]] == ["Montaña Full Day"]
    assert dias[1]["actividades"][0]["duracion_horas"] == 12.0
