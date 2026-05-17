package com.example.inventariolt.repository

// CategoriaRepository.kt


import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario.CategoriaResponseDTO

class CategoriaRepository {
    suspend fun getAllCategorias(): Result<List<CategoriaResponseDTO>> {
        return try {
            val response = RetrofitClient.categoriaApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}