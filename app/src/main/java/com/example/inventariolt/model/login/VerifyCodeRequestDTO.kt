package com.example.inventariolt.model.login

data class VerifyCodeRequestDTO(
    val correo: String,
    val codigo: String
)