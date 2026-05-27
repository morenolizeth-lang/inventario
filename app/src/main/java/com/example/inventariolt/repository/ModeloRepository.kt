package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario_Empleado.ModeloRequestDTO
import com.example.inventariolt.model.inventario_Empleado.ModeloResponseDTO

class ModeloRepository {
    suspend fun getAllModelos(): Result<List<ModeloResponseDTO>> {
        return try {
            val response = RetrofitClient.modeloApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getModelosByTienda(tiendaId: Long): Result<List<ModeloResponseDTO>> {
        return try {
            val response = RetrofitClient.modeloApi.getAll()
            // El DTO de Modelo no tiene tiendaId, se devuelven todos
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createModelo(request: ModeloRequestDTO): Result<ModeloResponseDTO> {
        return try {
            val response = RetrofitClient.modeloApi.create(request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun getModeloById(id: Long): Result<ModeloResponseDTO> {
        return try {
            val response = RetrofitClient.modeloApi.getById(id)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun updateModelo(id: Long, request: ModeloRequestDTO): Result<ModeloResponseDTO> {
        return try {
            val response = RetrofitClient.modeloApi.update(id, request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }

    suspend fun deleteModelo(id: Long): Result<Unit> {
        return try {
            val response = RetrofitClient.modeloApi.delete(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = if (response.code() == 500) {
                    "No se puede borrar porque este modelo está en uso (tiene colores o productos asociados)"
                } else {
                    val errorBody = response.errorBody()?.string()
                    "Error ${response.code()}: ${errorBody ?: "Error al eliminar"}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}