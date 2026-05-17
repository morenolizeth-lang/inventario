package com.example.inventariolt.model.login

data class UsuarioRequestDTO(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String,
    val estado: Boolean,
    val tiendaId: Long?
)