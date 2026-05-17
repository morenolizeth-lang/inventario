package com.example.inventariolt.interfaces

import com.example.inventariolt.model.login.UsuarioRequestDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


interface UsuarioApi {
    @POST("api/usuarios")
    suspend fun create(@Body request: UsuarioRequestDTO): UsuarioResponseDTO

    @GET("api/usuarios")
    suspend fun getAll(): List<UsuarioResponseDTO>

    @GET("api/usuarios/{id}")
    suspend fun getById(@Path("id") id: Long): UsuarioResponseDTO

    @PUT("api/usuarios/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: UsuarioRequestDTO): UsuarioResponseDTO

    @Multipart
    @PATCH("api/usuarios/{id}/foto-perfil")
    suspend fun uploadFotoPerfil(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part
    ): UsuarioResponseDTO

    @DELETE("api/usuarios/{id}")
    suspend fun delete(@Path("id") id: Long): Response<Unit>
}
