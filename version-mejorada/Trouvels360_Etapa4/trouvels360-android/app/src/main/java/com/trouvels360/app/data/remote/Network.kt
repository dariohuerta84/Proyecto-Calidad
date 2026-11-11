package com.trouvels360.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.trouvels360.app.BuildConfig
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * CookieJar persistente: guarda "access_token" (JWT HttpOnly) y "XSRF-TOKEN"
 * en SharedPreferences para que la sesión sobreviva al cierre de la app,
 * igual que las cookies del navegador en la versión web.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookies", Context.MODE_PRIVATE)
    private val cookies = mutableMapOf<String, Cookie>()

    init {
        load()
    }

    private fun key(c: Cookie) = "${c.domain}|${c.path}|${c.name}"

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val now = System.currentTimeMillis()
        cookies.forEach { c ->
            if (c.expiresAt < now || c.value.isEmpty()) this.cookies.remove(key(c))
            else this.cookies[key(c)] = c
        }
        persist(url)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        this.cookies.values.removeAll { it.expiresAt < now }
        return this.cookies.values.filter { it.matches(url) }
    }

    /** Valor de una cookie (p. ej. XSRF-TOKEN) válida para la URL dada. */
    @Synchronized
    fun valueFor(url: HttpUrl, name: String): String? =
        loadForRequest(url).firstOrNull { it.name == name }?.value

    @Synchronized
    fun clear() {
        cookies.clear()
        prefs.edit().clear().apply()
    }

    private fun persist(url: HttpUrl) {
        val arr = JSONArray()
        cookies.values.forEach { arr.put(it.toString()) }
        prefs.edit()
            .putString("cookies", arr.toString())
            .putString("base", "${url.scheme}://${url.host}:${url.port}/")
            .apply()
    }

    private fun load() {
        val raw = prefs.getString("cookies", null) ?: return
        val base = prefs.getString("base", null) ?: return
        val baseUrl = base.toHttpUrlOrNull() ?: return
        val arr = runCatching { JSONArray(raw) }.getOrNull() ?: return
        for (i in 0 until arr.length()) {
            Cookie.parse(baseUrl, arr.optString(i))?.let { cookies[key(it)] = it }
        }
    }
}

/**
 * Double-submit CSRF: el backend compara la cookie XSRF-TOKEN con el header X-XSRF-TOKEN
 * en POST/PUT/PATCH/DELETE (middleware VerifyApiCsrf).
 */
class CsrfInterceptor(private val jar: PersistentCookieJar) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val builder = req.newBuilder().header("Accept", "application/json")
        if (req.method in setOf("POST", "PUT", "PATCH", "DELETE")) {
            jar.valueFor(req.url, "XSRF-TOKEN")?.let { builder.header("X-XSRF-TOKEN", it) }
        }
        return chain.proceed(builder.build())
    }
}

/**
 * Laravel a veces serializa colecciones filtradas como objeto {"0":{...},"2":{...}}
 * en vez de arreglo. Este adapter acepta ambos formatos para cualquier List<T>.
 */
class LenientListAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (!List::class.java.isAssignableFrom(type.rawType)) return null
        val delegate = gson.getDelegateAdapter(this, type)
        val rawArg = (type.type as? java.lang.reflect.ParameterizedType)
            ?.actualTypeArguments?.firstOrNull() ?: return delegate
        val elementType = if (rawArg is java.lang.reflect.WildcardType) rawArg.upperBounds.first() else rawArg
        val elementAdapter = gson.getAdapter(TypeToken.get(elementType))

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) = delegate.write(out, value)

            @Suppress("UNCHECKED_CAST")
            override fun read(reader: JsonReader): T? = when (reader.peek()) {
                JsonToken.BEGIN_OBJECT -> {
                    val list = mutableListOf<Any?>()
                    reader.beginObject()
                    while (reader.hasNext()) {
                        reader.nextName()
                        list.add(elementAdapter.read(reader))
                    }
                    reader.endObject()
                    list as T
                }
                JsonToken.NULL -> { reader.nextNull(); null }
                else -> delegate.read(reader)
            }
        }
    }
}

object ApiClient {
    lateinit var api: ApiService
        private set
    lateinit var cookieJar: PersistentCookieJar
        private set
    lateinit var gson: Gson
        private set

    fun init(context: Context) {
        cookieJar = PersistentCookieJar(context.applicationContext)
        gson = GsonBuilder()
            .registerTypeAdapterFactory(LenientListAdapterFactory())
            .create()

        // BASIC: método, URL y código. Nunca BODY: el cuerpo lleva contraseñas y datos de tarjeta.
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(CsrfInterceptor(cookieJar))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        api = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}
