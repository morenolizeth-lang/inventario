package com.example.inventariolt.interfaces

import com.example.inventariolt.model.inventario.GeneroRequestDTO
import com.example.inventariolt.model.inventario.GeneroResponseDTO
import retrofit2.Response
import retrofit2.http.*


interface GeneroApi {
    @POST("api/generos")
    suspend fun create(@Body request: GeneroRequestDTO): GeneroResponseDTO

    @GET("api/generos")
    suspend fun getAll(): List<GeneroResponseDTO>

    @GET("api/generos/{id}")
    suspend fun getById(@Path("id") id: Long): GeneroResponseDTO

    @PUT("api/generos/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: GeneroRequestDTO): GeneroResponseDTO

    @DELETE("api/generos/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
