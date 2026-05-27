package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualRequestDTO
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualResponseDTO
import okhttp3.MultipartBody

class VarianteVisualRepository {
    suspend fun getAllVariantes(): Result<List<VarianteVisualResponseDTO>> {
        return try {
            val response = RetrofitClient.varianteVisualApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVariantesByTienda(tiendaId: Long): Result<List<VarianteVisualResponseDTO>> {
        return try {
            val response = RetrofitClient.varianteVisualApi.getAll()
            // El DTO de VarianteVisual no tiene tiendaId, se devuelven todos
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createVariante(request: VarianteVisualRequestDTO): Result<VarianteVisualResponseDTO> {
        return try {
            val response = RetrofitClient.varianteVisualApi.create(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImagen(varianteId: Long, file: MultipartBody.Part): Result<VarianteVisualResponseDTO> {
        return try {
            val response = RetrofitClient.varianteVisualApi.uploadImagen(varianteId, file)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVariante(id: Long, request: VarianteVisualRequestDTO): Result<VarianteVisualResponseDTO> {
        return try {
            val response = RetrofitClient.varianteVisualApi.update(id, request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVariante(id: Long): Result<Unit> {
        return try {
            val response = RetrofitClient.varianteVisualApi.delete(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = if (response.code() == 500) {
                    "No se puede borrar porque esta variante está en uso (tiene stock o productos asociados)"
                } else {
                    val errorBody = response.errorBody()?.string()
                    "Error ${response.code()}: ${errorBody ?: "Error al eliminar"}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVarianteById(id: Long): Result<VarianteVisualResponseDTO> {
        return try {
            val response = RetrofitClient.varianteVisualApi.getById(id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
