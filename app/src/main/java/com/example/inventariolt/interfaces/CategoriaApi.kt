package com.example.inventariolt.interfaces

import com.example.inventariolt.model.inventario.CategoriaRequestDTO
import com.example.inventariolt.model.inventario.CategoriaResponseDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

// ----------------------------------------------------------------------
// 1. Categorías
// ----------------------------------------------------------------------
interface CategoriaApi {
    @POST("/api/categorias")
    suspend fun create(@Body request: CategoriaRequestDTO): CategoriaResponseDTO

    @GET("/api/categorias")
    suspend fun getAll(): List<CategoriaResponseDTO>

    @GET("/api/categorias/{id}")
    suspend fun getById(@Path("id") id: Long): CategoriaResponseDTO

    @PUT("/api/categorias/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: CategoriaRequestDTO): CategoriaResponseDTO

    @DELETE("/api/categorias/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
