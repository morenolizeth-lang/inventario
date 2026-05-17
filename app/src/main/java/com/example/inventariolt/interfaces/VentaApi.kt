package com.example.inventariolt.interfaces

import com.example.inventariolt.model.inventario.VentaRequestDTO
import com.example.inventariolt.model.inventario.VentaResponseDTO
import retrofit2.Response
import retrofit2.http.*

interface VentaApi {
    @POST("api/ventas")
    suspend fun create(@Body request: VentaRequestDTO): VentaResponseDTO

    @GET("api/ventas")
    suspend fun getAll(): List<VentaResponseDTO>

    @GET("api/ventas/{id}")
    suspend fun getById(@Path("id") id: Long): VentaResponseDTO

    @PUT("api/ventas/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: VentaRequestDTO): VentaResponseDTO

    @DELETE("api/ventas/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}