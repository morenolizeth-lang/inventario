package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario.VentaRequestDTO
import com.example.inventariolt.model.inventario.VentaResponseDTO

class VentaRepository {
    suspend fun createVenta(request: VentaRequestDTO): Result<VentaResponseDTO> {
        return try {
            val response = RetrofitClient.ventaApi.create(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllVentas(): Result<List<VentaResponseDTO>> {
        return try {
            val response = RetrofitClient.ventaApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
