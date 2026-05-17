package com.example.inventariolt.interfaces

import com.example.inventariolt.model.login.SendCodeRequestDTO
import com.example.inventariolt.model.login.VerifyCodeRequestDTO
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationApi {
    @POST("api/verification/send-code")
    suspend fun sendCode(@Body request: SendCodeRequestDTO): ResponseBody  // ← Cambiar a ResponseBody

    @POST("api/verification/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequestDTO): ResponseBody  // ← Cambiar a ResponseBody
}