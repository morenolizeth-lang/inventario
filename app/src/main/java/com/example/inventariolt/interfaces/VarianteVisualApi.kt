package com.example.inventariolt.interfaces

import com.example.inventariolt.model.inventario_Empleado.VarianteVisualRequestDTO
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualResponseDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


interface VarianteVisualApi {
    @POST("api/variantes-visuales")
    suspend fun create(@Body request: VarianteVisualRequestDTO): VarianteVisualResponseDTO

    @GET("api/variantes-visuales")
    suspend fun getAll(): List<VarianteVisualResponseDTO>

    @GET("api/variantes-visuales/{id}")
    suspend fun getById(@Path("id") id: Long): VarianteVisualResponseDTO

    @PUT("api/variantes-visuales/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: VarianteVisualRequestDTO): VarianteVisualResponseDTO

    @Multipart
    @PATCH("api/variantes-visuales/{id}/imagen")
    suspend fun uploadImagen(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): VarianteVisualResponseDTO

    @DELETE("api/variantes-visuales/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}