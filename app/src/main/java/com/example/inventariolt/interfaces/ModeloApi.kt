package com.example.inventariolt.interfaces


import com.example.inventariolt.model.inventario.ModeloRequestDTO
import com.example.inventariolt.model.inventario.ModeloResponseDTO
import retrofit2.Response
import retrofit2.http.*


interface ModeloApi {
    @POST("api/modelos")
    suspend fun create(@Body request: ModeloRequestDTO): ModeloResponseDTO

    @GET("api/modelos")
    suspend fun getAll(): List<ModeloResponseDTO>

    @GET("api/modelos/{id}")
    suspend fun getById(@Path("id") id: Long): ModeloResponseDTO

    @PUT("api/modelos/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: ModeloRequestDTO): ModeloResponseDTO

    @DELETE("api/modelos/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}