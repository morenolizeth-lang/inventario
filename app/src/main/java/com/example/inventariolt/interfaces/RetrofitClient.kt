package com.example.inventariolt.interfaces

import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://backent-tienda-de-zapatos.onrender.com/"

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ ->
            com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ ->
            LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        })
        .create()

    private val client = OkHttpClient.Builder()
        .addInterceptor(BrotliInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "es-ES,es;q=0.8,en;q=0.5")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Origin", BASE_URL)
                .header("Referer", BASE_URL)
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val verificationApi: VerificationApi by lazy {
        retrofit.create(VerificationApi::class.java)
    }
    val tiendaApi: TiendaApi by lazy {
        retrofit.create(TiendaApi::class.java)
    }
    val usuarioApi: UsuarioApi by lazy {
        retrofit.create(UsuarioApi::class.java)
    }

    // En RetrofitClient.kt, agrega:
    val categoriaApi: CategoriaApi by lazy { retrofit.create(CategoriaApi::class.java) }
    val marcaApi: MarcaApi by lazy { retrofit.create(MarcaApi::class.java) }
    val generoApi: GeneroApi by lazy { retrofit.create(GeneroApi::class.java) }
    val colorApi: ColorApi by lazy { retrofit.create(ColorApi::class.java) }
    val modeloApi: ModeloApi by lazy { retrofit.create(ModeloApi::class.java) }
    val varianteVisualApi: VarianteVisualApi by lazy { retrofit.create(VarianteVisualApi::class.java) }
    val productoApi: ProductoApi by lazy { retrofit.create(ProductoApi::class.java) }
    val ventaApi: VentaApi by lazy { retrofit.create(VentaApi::class.java) }
}