package com.example.inventariolt.model.login

data class UsuarioUpdateDTO(
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: Boolean,
    val tiendaId: Long?
)