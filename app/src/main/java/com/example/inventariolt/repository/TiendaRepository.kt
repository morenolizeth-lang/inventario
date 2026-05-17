// TiendaRepository.kt
package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.login.TiendaRequestDTO
import com.example.inventariolt.model.login.TiendaResponseDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

class TiendaRepository {

    suspend fun createTienda(
        nombre: String,
        direccion: String,
        telefono: String
    ): Result<TiendaResponseDTO> {
        return try {
            val request = TiendaRequestDTO(nombre, direccion, telefono)
            val response = RetrofitClient.tiendaApi.createTienda(request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getAllTiendas(): Result<List<TiendaResponseDTO>> {
        return try {
            println("🔍 DEBUG: Iniciando getAllTiendas()")

            val responseBody: ResponseBody = RetrofitClient.tiendaApi.getAllTiendas()
            val jsonString = responseBody.string()
            println("📦 Respuesta cruda: $jsonString")

            if (jsonString.isBlank()) {
                return Result.failure(Exception("Respuesta vacía del servidor"))
            }

            val gson = Gson()

            if (jsonString.trim().startsWith("[")) {
                val type = object : TypeToken<List<TiendaResponseDTO>>() {}.type
                val tiendas: List<TiendaResponseDTO> = gson.fromJson(jsonString, type)
                println("✅ Tiendas encontradas: ${tiendas.size}")
                Result.success(tiendas)
            } else {
                Result.failure(Exception("Respuesta no es un array JSON: $jsonString"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("❌ HTTP Error ${e.code()}: $errorBody")
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getTiendaById(id: Long): Result<TiendaResponseDTO> {
        return try {
            val responseBody: ResponseBody = RetrofitClient.tiendaApi.getTiendaById(id)
            val jsonString = responseBody.string()
            val gson = Gson()
            val tienda: TiendaResponseDTO = gson.fromJson(jsonString, TiendaResponseDTO::class.java)
            Result.success(tienda)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun updateTienda(
        id: Long,
        nombre: String,
        direccion: String,
        telefono: String
    ): Result<TiendaResponseDTO> {
        return try {
            val request = TiendaRequestDTO(nombre, direccion, telefono)
            val response = RetrofitClient.tiendaApi.updateTienda(id, request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun deleteTienda(id: Long): Result<Unit> {
        return try {
            val response = RetrofitClient.tiendaApi.deleteTienda(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: No se pudo eliminar"))
            }
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}