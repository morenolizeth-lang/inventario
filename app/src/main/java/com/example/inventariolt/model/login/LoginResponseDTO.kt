package com.example.inventariolt.model.login

import com.google.gson.annotations.SerializedName

data class LoginResponseDTO(
    @SerializedName("idUsuario")
    val idUsuario: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: Boolean,
    val tiendaId: Long?
)