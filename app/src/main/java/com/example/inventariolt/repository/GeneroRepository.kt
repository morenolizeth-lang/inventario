package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario.GeneroResponseDTO

class GeneroRepository {
    suspend fun getAllGeneros(): Result<List<GeneroResponseDTO>> {
        return try {
            val response = RetrofitClient.generoApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}