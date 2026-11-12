#!/usr/bin/env python3
"""
Prueba rápida de concurrencia SIN JMeter (solo Python 3, sin librerías externas).
Reproduce TC-PERF-003 y TC-PERF-005 para comprobar el backend antes de lanzar el plan JMeter:

  TC-PERF-003  N viajeros piden a la vez la última unidad de un hotel  -> exactamente 1x201 y (N-1)x409
  TC-PERF-005  M viajeros reservan a la vez una salida de C cupos      -> personas confirmadas == C (nunca más)

Uso:  python concurrencia_smoke.py [--api http://localhost:8000/api] [--hilos-hotel 20] [--hilos-tour 30] [--cupo 10]
Requiere el QaSeeder (qa.viajero1..30 / Viajero123, edwinproveedor@gmail.com / proveedor) y
LOGIN_RATE_LIMIT alto en el .env del entorno de pruebas.
"""
import argparse, http.cookiejar, json, random, string, sys, threading, time, urllib.error, urllib.parse, urllib.request, uuid
from datetime import date, timedelta

PASS_VIAJERO = "Viajero123"
PROVEEDOR = ("edwinproveedor@gmail.com", "proveedor")
PAGO = {"titular": "QA JMeter", "numero": "4242424242424242", "vencimiento": "12/39", "cvc": "123"}


class Cliente:
    """Sesión HTTP con cookies propias (JWT HttpOnly + XSRF de doble envío)."""

    def __init__(self, api):
        self.api = api.rstrip("/")
        self.jar = http.cookiejar.CookieJar()
        self.op = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.jar))

    def xsrf(self):
        for c in self.jar:
            if c.name == "XSRF-TOKEN":
                return urllib.parse.unquote(c.value)
        return ""

    def req(self, metodo, ruta, cuerpo=None, cabeceras=None):
        h = {"Accept": "application/json", "Content-Type": "application/json", "X-XSRF-TOKEN": self.xsrf()}
        h.update(cabeceras or {})
        datos = json.dumps(cuerpo).encode() if cuerpo is not None else None
        r = urllib.request.Request(self.api + ruta, data=datos, headers=h, method=metodo)
        t0 = time.perf_counter()
        try:
            with self.op.open(r, timeout=60) as resp:
                cuerpo_txt = resp.read().decode() or "null"
                return resp.status, json.loads(cuerpo_txt), (time.perf_counter() - t0) * 1000
        except urllib.error.HTTPError as e:
            txt = e.read().decode() or "null"
            try:
                return e.code, json.loads(txt), (time.perf_counter() - t0) * 1000
            except ValueError:
                return e.code, {"raw": txt[:200]}, (time.perf_counter() - t0) * 1000

    def login(self, email, password):
        self.req("GET", "/auth/csrf")
        st, body, _ = self.req("POST", "/auth/login", {"email": email, "password": password})
        if st != 200:
            raise SystemExit(f"Login falló para {email}: {st} {body}")
        return self


def ciudad_unica():
    return "PERF-" + "".join(random.choices(string.ascii_uppercase + string.digits, k=8))


def lanzar(sesiones, accion):
    """Ejecuta accion(sesion) en paralelo; todos los hilos salen juntos (equivale al Synchronizing Timer)."""
    barrera = threading.Barrier(len(sesiones))
    resultados = [None] * len(sesiones)

    def hilo(i, s):
        barrera.wait()
        resultados[i] = accion(s)

    hs = [threading.Thread(target=hilo, args=(i, s)) for i, s in enumerate(sesiones)]
    [h.start() for h in hs]
    [h.join() for h in hs]
    return resultados


def ultima_habitacion(api, n):
    prov = Cliente(api).login(*PROVEEDOR)
    ciudad = ciudad_unica()
    st, body, _ = prov.req("POST", "/hoteles", {
        "nombre": f"Hotel {ciudad}", "ciudad": ciudad, "pais": "Perú", "imagen_url": "https://example.com/p.jpg",
        "direccion": "Av. Carga 1", "estrellas": 3,
        "habitaciones": [{"nombre": "Última", "capacidad_adultos": 2, "capacidad_ninos": 0, "cantidad": 1, "precio_por_noche": 100}]})
    assert st == 201, (st, body)
    hotel_id = body["data"].get("servicio_id") or body["data"]["id"]
    _, det, _ = prov.req("GET", f"/hoteles/{hotel_id}")
    hab_id = det["habitaciones"][0]["id"]
    d = date.today() + timedelta(days=random.randint(400, 3000))
    viajeros = [Cliente(api).login(f"qa.viajero{i}@trouvels360.test", PASS_VIAJERO) for i in range(1, n + 1)]
    cuerpo = {"fecha_inicio": d.isoformat(), "fecha_fin": (d + timedelta(days=1)).isoformat(),
              "items": [{"habitacion_id": hab_id, "cantidad": 1}], "pago": PAGO}
    res = lanzar(viajeros, lambda s: s.req("POST", "/reservas-habitaciones", cuerpo, {"Idempotency-Key": str(uuid.uuid4())}))
    codigos = [r[0] for r in res]
    ok = codigos.count(201) == 1 and codigos.count(409) == n - 1
    print(f"TC-PERF-003  {n} hilos por la última unidad -> 201: {codigos.count(201)}  409: {codigos.count(409)}  "
          f"otros: {[c for c in codigos if c not in (201, 409)]}  p95: {p95([r[2] for r in res]):.0f} ms  -> {'OK' if ok else 'FALLA'}")
    return ok


def cupos_tour(api, m, cupo):
    prov = Cliente(api).login(*PROVEEDOR)
    ciudad = ciudad_unica()
    d = date.today() + timedelta(days=random.randint(400, 3000))
    st, body, _ = prov.req("POST", "/tours", {
        "nombre": f"Tour {ciudad}", "descripcion": "Carga", "ciudad": ciudad, "pais": "Perú", "imagen_url": "https://example.com/t.jpg",
        "categoria": "Aventura", "duracion": 60, "precio": 50, "salidas": [{"fecha": d.isoformat(), "hora": "08:00", "cupo_total": cupo}]})
    assert st == 201, (st, body)
    tour_id = body["data"].get("servicio_id") or body["data"]["id"]
    _, salidas, _ = prov.req("GET", f"/tours/{tour_id}/salidas")
    salida_id = salidas[0]["id"]
    viajeros = [Cliente(api).login(f"qa.viajero{(i % 30) + 1}@trouvels360.test", PASS_VIAJERO) for i in range(m)]
    res = lanzar(viajeros, lambda s: s.req("POST", f"/tours/salidas/{salida_id}/reservas", {"personas": 1, "pago": PAGO},
                                           {"Idempotency-Key": str(uuid.uuid4())}))
    codigos = [r[0] for r in res]
    _, salidas, _ = prov.req("GET", f"/tours/{tour_id}/salidas")
    reservado = salidas[0]["cupo_reservado"]
    ok = codigos.count(201) == cupo and reservado == cupo and all(c in (201, 409) for c in codigos)
    print(f"TC-PERF-005  {m} hilos sobre {cupo} cupos -> 201: {codigos.count(201)}  409: {codigos.count(409)}  "
          f"otros: {[c for c in codigos if c not in (201, 409)]}  cupo_reservado final: {reservado}  -> {'OK' if ok else 'FALLA'}")
    return ok


def p95(v):
    v = sorted(v)
    return v[max(0, int(round(0.95 * len(v))) - 1)] if v else 0


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--api", default="http://localhost:8000/api")
    ap.add_argument("--hilos-hotel", type=int, default=20)
    ap.add_argument("--hilos-tour", type=int, default=30)
    ap.add_argument("--cupo", type=int, default=10)
    a = ap.parse_args()
    r1 = ultima_habitacion(a.api, a.hilos_hotel)
    r2 = cupos_tour(a.api, a.hilos_tour, a.cupo)
    sys.exit(0 if r1 and r2 else 1)
