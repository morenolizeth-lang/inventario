package com.example.inventariolt.interfaces

import com.example.inventariolt.model.login.LoginRequestDTO
import com.example.inventariolt.model.login.LoginResponseDTO
import com.example.inventariolt.model.login.RegisterRequestDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun registrar(
        @Body request: RegisterRequestDTO
    ): UsuarioResponseDTO

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDTO
    ): LoginResponseDTO
}
