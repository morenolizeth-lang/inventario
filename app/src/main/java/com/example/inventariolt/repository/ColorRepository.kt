package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario_Empleado.ColorResponseDTO

class ColorRepository {
    suspend fun getAllColores(): Result<List<ColorResponseDTO>> {
        return try {
            val response = RetrofitClient.colorApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}