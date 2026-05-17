package com.example.inventariolt.model.login

data class RegisterRequestDTO(
    val nombre: String,
    val correo: String,
    val password: String,
    val codigo: String,
    val tiendaId: Long? = null // Añadimos el ID de la tienda
)