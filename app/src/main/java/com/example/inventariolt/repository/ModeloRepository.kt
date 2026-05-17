package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario.ModeloRequestDTO
import com.example.inventariolt.model.inventario.ModeloResponseDTO

class ModeloRepository {
    suspend fun getAllModelos(): Result<List<ModeloResponseDTO>> {
        return try {
            val response = RetrofitClient.modeloApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createModelo(request: ModeloRequestDTO): Result<ModeloResponseDTO> {
        return try {
            val response = RetrofitClient.modeloApi.create(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}