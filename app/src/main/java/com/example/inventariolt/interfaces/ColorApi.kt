package com.example.inventariolt.interfaces

import com.example.inventariolt.model.inventario_Empleado.ColorRequestDTO
import com.example.inventariolt.model.inventario_Empleado.ColorResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ColorApi {
    @POST("api/colores")
    suspend fun create(@Body request: ColorRequestDTO): ColorResponseDTO

    @GET("api/colores")
    suspend fun getAll(): List<ColorResponseDTO>

    @GET("api/colores/{id}")
    suspend fun getById(@Path("id") id: Long): ColorResponseDTO

    @PUT("api/colores/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: ColorRequestDTO): ColorResponseDTO

    @DELETE("api/colores/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}