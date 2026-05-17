package com.example.inventariolt.repository

import android.content.Context
import android.net.Uri
import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.login.LoginRequestDTO
import com.example.inventariolt.model.login.LoginResponseDTO
import com.example.inventariolt.model.login.RegisterRequestDTO
import com.example.inventariolt.model.login.SendCodeRequestDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import com.example.inventariolt.model.login.VerifyCodeRequestDTO
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody

import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AuthRepository {

    suspend fun sendCode(correo: String): Result<String> {
        return try {
            println("📧 Enviando código a: $correo")
            val request = SendCodeRequestDTO(correo)
            val response: ResponseBody = RetrofitClient.verificationApi.sendCode(request)
            val message = response.string()  // ← Convertir ResponseBody a String
            println("✅ Respuesta: $message")
            Result.success(message)
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Error al enviar código: ${e.message}"))
        }
    }

    suspend fun verifyCode(correo: String, codigo: String): Result<String> {
        return try {
            println("🔐 Verificando código: $codigo para $correo")
            val request = VerifyCodeRequestDTO(correo, codigo)
            val response: ResponseBody = RetrofitClient.verificationApi.verifyCode(request)
            val message = response.string()  // ← Convertir ResponseBody a String
            println("✅ Respuesta: $message")
            Result.success(message)
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Error al verificar código: ${e.message}"))
        }
    }

    // register y login se mantienen igual

    suspend fun register(
        context: Context,
        nombre: String,
        correo: String,
        password: String,
        codigo: String,
        tiendaId: Long? = 1L,
        imageUri: Uri? = null
    ): Result<UsuarioResponseDTO> {
        return try {
            val nombreLimpio = nombre.trim()
            val correoLimpio = correo.trim().lowercase()
            println("📝 Registrando usuario: '$nombreLimpio', '$correoLimpio'")
            
            val request = RegisterRequestDTO(
                nombre = nombreLimpio,
                correo = correoLimpio,
                password = password,
                codigo = codigo.trim(),
                tiendaId = tiendaId
            )
            val response = RetrofitClient.authApi.registrar(request)
            println("🔍 DEBUG REGISTRO: Respuesta completa -> $response")
            
            if (response.idUsuario == 0L) {
                println("⚠️ ERROR CRÍTICO: El ID recibido es 0. Verifique si el campo en el JSON del backend es realmente 'id'")
            }

            // Si hay una imagen seleccionada, subirla
            if (imageUri != null && response.idUsuario != 0L) {
                println("📸 Subiendo foto de perfil para el usuario ${response.idUsuario}...")
                uploadProfilePicture(context, response.idUsuario, imageUri)
            }

            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("❌ HTTP Error ${e.code()}: $errorBody")
            val errorMessage = extractErrorMessage(errorBody)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun login(correo: String, password: String): Result<LoginResponseDTO> {
        return try {
            println("🔑 Iniciando sesión: $correo")
            val request = LoginRequestDTO(correo, password)
            val response = RetrofitClient.authApi.login(request)
            println("✅ Login exitoso: ${response.nombre}")
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("❌ HTTP Error ${e.code()}: $errorBody")
            val errorMessage = extractErrorMessage(errorBody)
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun uploadProfilePicture(context: Context, userId: Long, imageUri: Uri): Result<UsuarioResponseDTO> {
        return try {
            val file = uriToFile(context, imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            val response = RetrofitClient.usuarioApi.uploadFotoPerfil(userId, body)
            Result.success(response)
        } catch (e: Exception) {
            println("❌ Error al subir imagen: ${e.message}")
            Result.failure(e)
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    // Función para extraer mensaje de error del backend
    private fun extractErrorMessage(errorBody: String?): String {
        if (errorBody.isNullOrBlank()) return "Error desconocido"

        return try {
            // Intentar parsear como JSON
            val json = org.json.JSONObject(errorBody)
            json.optString("message", errorBody)
        } catch (e: Exception) {
            // Si no es JSON, devolver el texto plano
            errorBody
        }
    }
    // En AuthRepository, agrega este método de prueba
    suspend fun sendCodeWithOkHttp(correo: String): Result<String> {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val json = "{\"correo\":\"$correo\"}"
            val body = RequestBody.create(
                "application/json".toMediaType(),
                json
            )

            val request = Request.Builder()
                .url("https://backent-tienda-de-zapatos.onrender.com/api/verification/send-code")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            println("✅ Código respuesta: ${response.code}")
            println("✅ Headers: ${response.headers}")
            println("✅ Body: $responseBody")

            when (response.code) {
                200 -> {
                    // Éxito
                    Result.success(responseBody)
                }
                400 -> {
                    // Error de validación (correo inválido, formato incorrecto)
                    val errorMessage = when {
                        responseBody.contains("registrado", ignoreCase = true) ->
                            "Este correo ya está registrado. Usa otro o inicia sesión."
                        responseBody.contains("inválido", ignoreCase = true) ||
                                responseBody.contains("invalido", ignoreCase = true) ->
                            "Correo electrónico inválido. Verifica el formato."
                        responseBody.contains("dominio", ignoreCase = true) ->
                            "Dominio de correo no soportado. Usa Gmail, Hotmail, Outlook, etc."
                        else -> "Error en la solicitud: $responseBody"
                    }
                    Result.failure(Exception(errorMessage))
                }
                409 -> {
                    // Conflicto - correo ya registrado
                    Result.failure(Exception("Este correo ya está registrado. Por favor inicia sesión o usa otro correo."))
                }
                500 -> {
                    // Error interno del servidor
                    val errorMessage = when {
                        responseBody.contains("registrado", ignoreCase = true) ->
                            "Este correo ya está registrado en el sistema."
                        responseBody.contains("duplicate", ignoreCase = true) ->
                            "El correo ya existe en la base de datos."
                        else -> "Error interno del servidor. Intenta más tarde."
                    }
                    Result.failure(Exception(errorMessage))
                }
                else -> {
                    Result.failure(Exception("Error ${response.code}: $responseBody"))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            println("❌ Error de red: No se puede conectar al servidor")
            Result.failure(Exception("Error de conexión. Verifica tu internet."))
        } catch (e: java.net.SocketTimeoutException) {
            println("❌ Timeout: El servidor no responde")
            Result.failure(Exception("Tiempo de espera agotado. Intenta nuevamente."))
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}