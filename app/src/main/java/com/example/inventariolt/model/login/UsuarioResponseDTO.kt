package com.example.inventariolt.model.login

import com.google.gson.annotations.SerializedName

data class UsuarioResponseDTO(
    @SerializedName("idUsuario")
    val idUsuario: Long,
    val nombre: String,
    val correo: String,
    val rol: String,
    val fotoPerfil: String?,  // Puede ser null si no tiene foto
    val estado: Boolean,
    val tiendaId: Long?,
    val tiendaNombre: String?
)
