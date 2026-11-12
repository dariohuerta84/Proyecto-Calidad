#!/usr/bin/env python3
"""Genera trouvels360.jmx (JMeter 5.6.x). Se versiona el .jmx; este script solo documenta/regenera la estructura."""
from xml.sax.saxutils import escape as esc

def s(n, v): return f'<stringProp name="{n}">{esc(str(v))}</stringProp>'
def b(n, v): return f'<boolProp name="{n}">{"true" if v else "false"}</boolProp>'
def node(xml, *children):
    return xml + '<hashTree>' + ''.join(children) + '</hashTree>'

def args(pares):
    items = ''.join(f'<elementProp name="{esc(k)}" elementType="Argument">{s("Argument.name", k)}{s("Argument.value", v)}{s("Argument.metadata", "=")}</elementProp>' for k, v in pares)
    return f'<collectionProp name="Arguments.arguments">{items}</collectionProp>'

def http(nombre, metodo, ruta, cuerpo=None, *hijos):
    body = ''
    if cuerpo is not None:
        body = (b('HTTPSampler.postBodyRaw', True) +
                '<elementProp name="HTTPsampler.Arguments" elementType="Arguments"><collectionProp name="Arguments.arguments">'
                '<elementProp name="" elementType="HTTPArgument">' + b('HTTPArgument.always_encode', False) +
                s('Argument.value', cuerpo) + s('Argument.metadata', '=') + '</elementProp></collectionProp></elementProp>')
    else:
        body = '<elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" enabled="true"><collectionProp name="Arguments.arguments"/></elementProp>'
    x = (f'<HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="{esc(nombre)}" enabled="true">'
         + body + s('HTTPSampler.path', ruta) + s('HTTPSampler.method', metodo) + s('HTTPSampler.contentEncoding', 'UTF-8')
         + b('HTTPSampler.follow_redirects', True) + b('HTTPSampler.use_keepalive', True) + '</HTTPSamplerProxy>')
    return node(x, *hijos)

def codigo(nombre, patron, regex=False):
    tipo = 1 if regex else 8
    return node(f'<ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="{esc(nombre)}" enabled="true">'
                f'<collectionProp name="Asserion.test_strings">{s("p", patron)}</collectionProp>'
                + s('Assertion.custom_message', f'Se esperaba código {patron}') + s('Assertion.test_field', 'Assertion.response_code')
                + b('Assertion.assume_success', True) + f'<intProp name="Assertion.test_type">{tipo}</intProp></ResponseAssertion>')

def jsonpath(nombre, ruta, esperado='', regex=True):
    return node(f'<JSONPathAssertion guiclass="JSONPathAssertionGui" testclass="JSONPathAssertion" testname="{esc(nombre)}" enabled="true">'
                + s('JSON_PATH', ruta) + s('EXPECTED_VALUE', esperado) + b('JSONVALIDATION', bool(esperado))
                + b('EXPECT_NULL', False) + b('INVERT', False) + b('ISREGEX', regex) + '</JSONPathAssertion>')

def duracion(ms):
    return node(f'<DurationAssertion guiclass="DurationAssertionGui" testclass="DurationAssertion" testname="Duración &lt;= {ms} ms" enabled="true">'
                + s('DurationAssertion.duration', ms) + '</DurationAssertion>')

def extraer(var, ruta, defecto='NOT_FOUND'):
    return node(f'<JSONPostProcessor guiclass="JSONPostProcessorGui" testclass="JSONPostProcessor" testname="Extraer {var}" enabled="true">'
                + s('JSONPostProcessor.referenceNames', var) + s('JSONPostProcessor.jsonPathExprs', ruta)
                + s('JSONPostProcessor.match_numbers', '1') + s('JSONPostProcessor.defaultValues', defecto) + '</JSONPostProcessor>')

def jsr(tipo, nombre, script):
    gui = 'TestBeanGUI'
    return node(f'<{tipo} guiclass="{gui}" testclass="{tipo}" testname="{esc(nombre)}" enabled="true">'
                + s('scriptLanguage', 'groovy') + s('parameters', '') + s('filename', '') + s('cacheKey', 'true')
                + s('script', script) + f'</{tipo}>')

def csvds(nombre, archivo, compartir='shareMode.all'):
    return node(f'<CSVDataSet guiclass="TestBeanGUI" testclass="CSVDataSet" testname="{esc(nombre)}" enabled="true">'
                + s('filename', archivo) + s('fileEncoding', 'UTF-8') + s('variableNames', '') + b('ignoreFirstLine', False)
                + s('delimiter', ',') + b('quotedData', False) + b('recycle', True) + b('stopThread', False)
                + s('shareMode', compartir) + '</CSVDataSet>')

def grupo(clase, nombre, hilos, rampa, bucles='1', duracion_s=None, *hijos, comentario=''):
    gui = {'ThreadGroup': 'ThreadGroupGui', 'SetupThreadGroup': 'SetupThreadGroupGui', 'PostThreadGroup': 'PostThreadGroupGui'}[clase]
    x = (f'<{clase} guiclass="{gui}" testclass="{clase}" testname="{esc(nombre)}" enabled="true">'
         + s('TestPlan.comments', comentario) + s('ThreadGroup.on_sample_error', 'continue')
         + '<elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" testname="Loop Controller" enabled="true">'
         + b('LoopController.continue_forever', False) + s('LoopController.loops', bucles) + '</elementProp>'
         + s('ThreadGroup.num_threads', hilos) + s('ThreadGroup.ramp_time', rampa)
         + b('ThreadGroup.scheduler', duracion_s is not None) + s('ThreadGroup.duration', duracion_s or '') + s('ThreadGroup.delay', '')
         + b('ThreadGroup.same_user_on_next_iteration', True) + f'</{clase}>')
    return node(x, *hijos)

def una_vez(nombre, *hijos):
    return node(f'<OnceOnlyController guiclass="OnceOnlyControllerGui" testclass="OnceOnlyController" testname="{esc(nombre)}" enabled="true"/>', *hijos)

def si(nombre, cond, *hijos):
    return node(f'<IfController guiclass="IfControllerPanel" testclass="IfController" testname="{esc(nombre)}" enabled="true">'
                + s('IfController.condition', cond) + b('IfController.evaluateAll', False) + b('IfController.useExpression', True) + '</IfController>', *hijos)

def pausa(min_ms, rango_ms):
    return node(f'<UniformRandomTimer guiclass="UniformRandomTimerGui" testclass="UniformRandomTimer" testname="Tiempo de lectura {min_ms}-{min_ms + rango_ms} ms" enabled="true">'
                + s('ConstantTimer.delay', min_ms) + s('RandomTimer.range', rango_ms) + '</UniformRandomTimer>')

def sincronizar(nombre, prop, defecto):
    return node(f'<SyncTimer guiclass="TestBeanGUI" testclass="SyncTimer" testname="{esc(nombre)}" enabled="true">'
                + s('groupSize', f'${{__P({prop},{defecto})}}') + '<longProp name="timeoutInMs">60000</longProp></SyncTimer>')

SAVE = ('<objProp><name>saveConfig</name><value class="SampleSaveConfiguration"><time>true</time><latency>true</latency><timestamp>true</timestamp>'
        '<success>true</success><label>true</label><code>true</code><message>true</message><threadName>true</threadName><dataType>true</dataType>'
        '<encoding>false</encoding><assertions>true</assertions><subresults>true</subresults><responseData>false</responseData><samplerData>false</samplerData>'
        '<xml>false</xml><fieldNames>true</fieldNames><responseHeaders>false</responseHeaders><requestHeaders>false</requestHeaders>'
        '<responseDataOnError>false</responseDataOnError><saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>'
        '<assertionsResultsToSave>0</assertionsResultsToSave><bytes>true</bytes><sentBytes>true</sentBytes><url>true</url><threadCounts>true</threadCounts>'
        '<idleTime>true</idleTime><connectTime>true</connectTime></value></objProp>')

def oyente(gui, nombre, archivo=''):
    return node(f'<ResultCollector guiclass="{gui}" testclass="ResultCollector" testname="{esc(nombre)}" enabled="true">'
                + b('ResultCollector.error_logging', False) + SAVE + s('filename', archivo) + '</ResultCollector>')

# ------------------------------------------------------------------ piezas comunes
XSRF = """// Copia la cookie XSRF-TOKEN a la variable ${xsrf} antes de cada petición (CSRF de doble envío)
def cm = sampler.getCookieManager()
if (cm != null) {
  for (int i = 0; i < cm.getCookieCount(); i++) {
    def c = cm.get(i)
    if (c.getName() == 'XSRF-TOKEN') { vars.put('xsrf', java.net.URLDecoder.decode(c.getValue(), 'UTF-8')) }
  }
}
if (vars.get('xsrf') == null) { vars.put('xsrf', '') }"""

def login(prefijo, email, clave, maximo_ms=None):
    extra = [duracion(maximo_ms)] if maximo_ms else []
    return [
        http(f'{prefijo} GET /auth/csrf', 'GET', '/api/auth/csrf', None, codigo('Código 200', '200')),
        http(f'{prefijo} POST /auth/login', 'POST', '/api/auth/login', f'{{"email":"{email}","password":"{clave}"}}',
             codigo('Código 200', '200'), jsonpath('Devuelve el usuario', '$.data.user.email', '.+'), *extra),
    ]

def contar(clave):
    return jsr('JSR223PostProcessor', 'Contar códigos de respuesta', f"""def k = '{clave}.' + prev.getResponseCode()
synchronized (props) {{ props.put(k, ((props.get(k) ?: 0) as int) + 1) }}""")

PAGO = '"pago":{"titular":"QA JMeter","numero":"4242424242424242","vencimiento":"12/39","cvc":"123"}'
CIUDAD = 'PERF-${__RandomString(8,ABCDEFGHJKLMNPQRSTUVWXYZ23456789,)}'
FECHA = "${__groovy(java.time.LocalDate.now().plusDays(400 + new Random().nextInt(2500)).toString(),fecha_ini)}"

# ------------------------------------------------------------------ setUp: datos propios de la corrida
setup = grupo('SetupThreadGroup', 'setUp - Preparar datos de carrera (hotel con 1 unidad y tour con cupo limitado)', 1, 1, '1', None,
    *login('setUp', '${PROV_EMAIL}', '${PROV_PASS}'),
    http('setUp POST /hoteles (1 unidad)', 'POST', '/api/hoteles',
         '{"nombre":"Hotel ' + CIUDAD + '","ciudad":"PERF","pais":"Perú","imagen_url":"https://example.com/perf.jpg","direccion":"Av. Carga 1","estrellas":3,'
         '"habitaciones":[{"nombre":"Última","capacidad_adultos":2,"capacidad_ninos":0,"cantidad":1,"precio_por_noche":100}]}',
         codigo('Código 201', '201'), extraer('hotel_id', '$.data.servicio_id')),
    http('setUp GET /hoteles/{id}', 'GET', '/api/hoteles/${hotel_id}', None, codigo('Código 200', '200'), extraer('hab_id', '$.habitaciones[0].id')),
    http('setUp POST /tours (cupo limitado)', 'POST', '/api/tours',
         '{"nombre":"Tour ' + CIUDAD + '","descripcion":"Carga","ciudad":"PERF","pais":"Perú","imagen_url":"https://example.com/t.jpg",'
         '"categoria":"Aventura","duracion":60,"precio":50,"salidas":[{"fecha":"' + FECHA + '","hora":"08:00","cupo_total":${__P(cupo.tour,10)}}]}',
         codigo('Código 201', '201'), extraer('tour_id', '$.data.id')),
    http('setUp GET /tours/{id}/salidas', 'GET', '/api/tours/${tour_id}/salidas', None, codigo('Código 200', '200'), extraer('salida_id', '$[0].id')),
    jsr('JSR223Sampler', 'setUp Publicar ids como propiedades', """def hoy = java.time.LocalDate.now().plusDays(400 + new Random().nextInt(2500))
props.put('perf.hab_id', vars.get('hab_id'))
props.put('perf.tour_id', vars.get('tour_id'))
props.put('perf.salida_id', vars.get('salida_id'))
props.put('perf.fecha_inicio', hoy.toString())
props.put('perf.fecha_fin', hoy.plusDays(1).toString())
['TC-PERF-003.201','TC-PERF-003.409','TC-PERF-005.201','TC-PERF-005.409'].each { props.put(it, 0) }
SampleResult.setResponseData("hab=${vars.get('hab_id')} salida=${vars.get('salida_id')} fechas=${hoy}", 'UTF-8')
SampleResult.setSuccessful(vars.get('hab_id') != 'NOT_FOUND' && vars.get('salida_id') != 'NOT_FOUND')"""),
    comentario='Crea datos nuevos en cada corrida para que las pruebas de carrera sean repetibles.')

# ------------------------------------------------------------------ TC-PERF-001 login
g_login = grupo('ThreadGroup', 'TC-PERF-001 Login concurrente (HU-V02) - 50 usuarios', '${__P(usuarios.login,50)}', '${__P(rampa.login,10)}', '${__P(iteraciones.login,3)}', None,
    csvds('CSV usuarios', 'datos/usuarios.csv'),
    http('TC-PERF-001 GET /auth/csrf', 'GET', '/api/auth/csrf', None, codigo('Código 200', '200')),
    http('TC-PERF-001 POST /auth/login', 'POST', '/api/auth/login', '{"email":"${email}","password":"${password}"}',
         codigo('Código 200', '200'), jsonpath('Rol viajero', '$.data.user.rol', 'viajero', False), duracion(800)),
    http('TC-PERF-001 GET /auth/me', 'GET', '/api/auth/me', None, codigo('Código 200', '200'), jsonpath('Es el mismo usuario', '$.data.email', '${email}', False)),
    http('TC-PERF-001 POST /auth/logout', 'POST', '/api/auth/logout', '{}', codigo('Código 200', '200')),
    pausa(500, 1000),
    comentario='Objetivo: p95 < 800 ms y 0 % de errores. Requiere LOGIN_RATE_LIMIT alto en el entorno de pruebas.')

# ------------------------------------------------------------------ TC-PERF-002 catálogo de hoteles
g_hoteles = grupo('ThreadGroup', 'TC-PERF-002 Catálogo de hoteles (HU-V05/HU-V06) - 100 usuarios', '${__P(usuarios.catalogo,100)}', '${__P(rampa.catalogo,60)}', '-1', '${__P(duracion,300)}',
    csvds('CSV ciudades', 'datos/ciudades.csv'),
    http('TC-PERF-002 GET /hoteles (búsqueda)', 'GET',
         '/api/hoteles?ciudad=${ciudad}&check_in=${__timeShift(yyyy-MM-dd,,P30D,,)}&check_out=${__timeShift(yyyy-MM-dd,,P32D,,)}&adultos=${adultos}&habitaciones=1&per_page=12',
         None, codigo('Código 200', '200'), jsonpath('Trae meta.total', '$.meta.total', '\\d+'), duracion(1000), extraer('hotel_id', '$.data[0].id')),
    si('Si hubo resultados', '${__jexl3("${hotel_id}" != "NOT_FOUND")}',
       http('TC-PERF-002 GET /hoteles/{id} (detalle)', 'GET', '/api/hoteles/${hotel_id}', None, codigo('Código 200', '200'), duracion(1000)),
       http('TC-PERF-002 GET /hoteles/{id}/disponibilidad', 'GET',
            '/api/hoteles/${hotel_id}/disponibilidad?check_in=${__timeShift(yyyy-MM-dd,,P30D,,)}&check_out=${__timeShift(yyyy-MM-dd,,P32D,,)}&adultos=${adultos}&habitaciones=1',
            None, codigo('Código 200', '200'), duracion(1000))),
    pausa(1000, 2000),
    comentario='Objetivo: p95 < 1 s con 100 usuarios, ramp-up 60 s, 5 minutos.')

# ------------------------------------------------------------------ TC-PERF-004 tours
g_tours = grupo('ThreadGroup', 'TC-PERF-004 Búsqueda de tours (HU-V08/HU-V09) - 100 usuarios', '${__P(usuarios.tours,100)}', '${__P(rampa.catalogo,60)}', '-1', '${__P(duracion,300)}',
    csvds('CSV categorías', 'datos/categorias.csv'),
    http('TC-PERF-004 GET /tours (filtros)', 'GET', '/api/tours?categoria=${__urlencode(${categoria})}&destino=${destino}&per_page=12',
         None, codigo('Código 200', '200'), jsonpath('Trae meta.total', '$.meta.total', '\\d+'), duracion(1000), extraer('tour_id', '$.data[0].id')),
    si('Si hubo resultados', '${__jexl3("${tour_id}" != "NOT_FOUND")}',
       http('TC-PERF-004 GET /tours/{id} (detalle)', 'GET', '/api/tours/${tour_id}', None, codigo('Código 200', '200'), duracion(1000))),
    pausa(1000, 2000),
    comentario='Objetivo: p95 < 1 s.')

# ------------------------------------------------------------------ TC-PERF-003 última habitación
g_ultima = grupo('ThreadGroup', 'TC-PERF-003 Carrera por la última habitación (HU-V07) - 20 hilos', '${__P(hilos.hotel,20)}', '1', '1', None,
    csvds('CSV usuarios (uno distinto por hilo)', 'datos/usuarios.csv'),
    *login('TC-PERF-003', '${email}', '${password}'),
    http('TC-PERF-003 POST /reservas-habitaciones (simultáneo)', 'POST', '/api/reservas-habitaciones',
         '{"fecha_inicio":"${__P(perf.fecha_inicio)}","fecha_fin":"${__P(perf.fecha_fin)}","items":[{"habitacion_id":${__P(perf.hab_id)},"cantidad":1}],' + PAGO + '}',
         node('<HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="Idempotency-Key único" enabled="true"><collectionProp name="HeaderManager.headers">'
              '<elementProp name="" elementType="Header">' + s('Header.name', 'Idempotency-Key') + s('Header.value', '${__UUID()}') + '</elementProp></collectionProp></HeaderManager>'),
         sincronizar('Synchronizing Timer: todos los hilos a la vez', 'hilos.hotel', 20),
         codigo('201 (ganador) o 409 (sin stock)', '201|409', regex=True), contar('TC-PERF-003')),
    comentario='Esperado: exactamente 1x201 y el resto 409. La verificación está en el tearDown.')

# ------------------------------------------------------------------ TC-PERF-005 cupos de tour
g_cupos = grupo('ThreadGroup', 'TC-PERF-005 Carrera por cupos de un tour (HU-V10) - 30 hilos', '${__P(hilos.tour,30)}', '1', '1', None,
    csvds('CSV usuarios', 'datos/usuarios.csv'),
    *login('TC-PERF-005', '${email}', '${password}'),
    http('TC-PERF-005 POST /tours/salidas/{id}/reservas (simultáneo)', 'POST', '/api/tours/salidas/${__P(perf.salida_id)}/reservas',
         '{"personas":1,' + PAGO + '}',
         node('<HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="Idempotency-Key único" enabled="true"><collectionProp name="HeaderManager.headers">'
              '<elementProp name="" elementType="Header">' + s('Header.name', 'Idempotency-Key') + s('Header.value', '${__UUID()}') + '</elementProp></collectionProp></HeaderManager>'),
         sincronizar('Synchronizing Timer: todos los hilos a la vez', 'hilos.tour', 30),
         codigo('201 (con cupo) o 409 (agotado)', '201|409', regex=True), contar('TC-PERF-005')),
    comentario='Esperado: personas confirmadas == cupo (10) y nunca más. La verificación está en el tearDown.')

# ------------------------------------------------------------------ TC-PERF-006 panel proveedor
g_panel = grupo('ThreadGroup', 'TC-PERF-006 Panel del proveedor (HU-P03/HU-P04/HU-P12) - 30 usuarios', '${__P(usuarios.panel,30)}', '${__P(rampa.panel,15)}', '${__P(iteraciones.panel,5)}', None,
    una_vez('Login una vez por hilo', *login('TC-PERF-006', '${PROV_EMAIL}', '${PROV_PASS}')),
    http('TC-PERF-006 GET /proveedor/indicadores', 'GET', '/api/proveedor/indicadores', None, codigo('Código 200', '200'),
         jsonpath('Trae conteos por estado', '$.data.reservas_hotel.pendiente', '\\d+'), duracion(1500)),
    http('TC-PERF-006 GET /proveedor/servicios', 'GET', '/api/proveedor/servicios?per_page=10', None, codigo('Código 200', '200'), duracion(1500)),
    http('TC-PERF-006 GET /proveedor/reservas (pendientes)', 'GET', '/api/proveedor/reservas?estado=pendiente&per_page=15', None, codigo('Código 200', '200'), duracion(1500)),
    pausa(1000, 2000),
    comentario='Objetivo: p95 < 1,5 s.')

# ------------------------------------------------------------------ tearDown: verificaciones de las carreras
teardown = grupo('PostThreadGroup', 'tearDown - Verificar resultados de las carreras', 1, 1, '1', None,
    jsr('JSR223Sampler', 'TC-PERF-003 Verificación: 1x201 y (N-1)x409', """int n = props.getProperty('hilos.hotel', '20') as int
int ok = (props.get('TC-PERF-003.201') ?: 0) as int
int sin = (props.get('TC-PERF-003.409') ?: 0) as int
SampleResult.setResponseData("hilos=${n} 201=${ok} 409=${sin}", 'UTF-8')
if (n == 0) { SampleResult.setResponseMessage('omitido'); return }
SampleResult.setSuccessful(ok == 1 && sin == n - 1)
SampleResult.setResponseMessage(ok == 1 && sin == n - 1 ? 'OK' : "Sobreventa o errores: 201=${ok} 409=${sin}")"""),
    *login('tearDown', '${PROV_EMAIL}', '${PROV_PASS}'),
    http('TC-PERF-005 Verificación: cupo_reservado == confirmadas <= cupo', 'GET', '/api/tours/${__P(perf.tour_id)}/salidas', None,
         jsr('JSR223Assertion', 'Sin sobreventa de cupos', """def s = new groovy.json.JsonSlurper().parseText(prev.getResponseDataAsString())[0]
int cupo = props.getProperty('cupo.tour', '10') as int
int ok = (props.get('TC-PERF-005.201') ?: 0) as int
int hilos = props.getProperty('hilos.tour', '30') as int
boolean bien = s.cupo_reservado <= cupo && s.cupo_reservado == ok && ok == Math.min(cupo, hilos)
if (!bien) { AssertionResult.setFailure(true); AssertionResult.setFailureMessage("cupo=${cupo} reservado=${s.cupo_reservado} confirmadas=${ok}") }""")),
    comentario='Marca como error en el reporte cualquier sobreventa.')

# ------------------------------------------------------------------ plan
plan = node(
    '<TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Trouvels360 - Pruebas de rendimiento y concurrencia (QA-02)" enabled="true">'
    + s('TestPlan.comments', 'TC-PERF-001..006. Ejecutar en modo no GUI: ver README.md. Parámetros con -J (host, port, usuarios.*, duracion...).')
    + b('TestPlan.functional_mode', False) + b('TestPlan.tearDown_on_shutdown', True) + b('TestPlan.serialize_threadgroups', True)
    + '<elementProp name="TestPlan.user_defined_variables" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="Variables" enabled="true">'
    + args([('PROTOCOL', '${__P(protocol,http)}'), ('HOST', '${__P(host,localhost)}'), ('PORT', '${__P(port,8000)}'),
            ('PROV_EMAIL', '${__P(proveedor.email,edwinproveedor@gmail.com)}'), ('PROV_PASS', '${__P(proveedor.password,proveedor)}')])
    + '</elementProp>' + s('TestPlan.user_define_classpath', '') + '</TestPlan>',
    node('<ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults" enabled="true">'
         '<elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" enabled="true"><collectionProp name="Arguments.arguments"/></elementProp>'
         + s('HTTPSampler.domain', '${HOST}') + s('HTTPSampler.port', '${PORT}') + s('HTTPSampler.protocol', '${PROTOCOL}')
         + s('HTTPSampler.contentEncoding', 'UTF-8') + s('HTTPSampler.connect_timeout', '10000') + s('HTTPSampler.response_timeout', '30000')
         + s('HTTPSampler.implementation', 'HttpClient4') + '</ConfigTestElement>'),
    node('<HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="Cabeceras: JSON + X-XSRF-TOKEN" enabled="true"><collectionProp name="HeaderManager.headers">'
         + ''.join(f'<elementProp name="" elementType="Header">{s("Header.name", k)}{s("Header.value", v)}</elementProp>'
                   for k, v in [('Accept', 'application/json'), ('Content-Type', 'application/json'), ('X-XSRF-TOKEN', '${xsrf}')])
         + '</collectionProp></HeaderManager>'),
    node('<CookieManager guiclass="CookiePanel" testclass="CookieManager" testname="HTTP Cookie Manager (JWT HttpOnly + XSRF-TOKEN, uno por hilo)" enabled="true">'
         '<collectionProp name="CookieManager.cookies"/>' + b('CookieManager.clearEachIteration', False) + b('CookieManager.controlledByThreadGroup', False) + '</CookieManager>'),
    jsr('JSR223PreProcessor', 'Copiar cookie XSRF-TOKEN a ${xsrf}', XSRF),
    setup, g_login, g_hoteles, g_tours, g_ultima, g_cupos, g_panel, teardown,
    oyente('SummaryReport', 'Summary Report'),
    oyente('StatVisualizer', 'Aggregate Report'),
    oyente('ViewResultsFullVisualizer', 'View Results Tree (solo errores, depuración)').replace('enabled="true"', 'enabled="false"', 1),
)

xml = '<?xml version="1.0" encoding="UTF-8"?>\n<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.6.3"><hashTree>' + plan + '</hashTree></jmeterTestPlan>\n'
open('trouvels360.jmx', 'w', encoding='utf-8').write(xml)
print('trouvels360.jmx generado')
