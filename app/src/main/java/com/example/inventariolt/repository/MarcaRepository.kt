package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario_Empleado.MarcaResponseDTO

class MarcaRepository {
    suspend fun getAllMarcas(): Result<List<MarcaResponseDTO>> {
        return try {
            val response = RetrofitClient.marcaApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}