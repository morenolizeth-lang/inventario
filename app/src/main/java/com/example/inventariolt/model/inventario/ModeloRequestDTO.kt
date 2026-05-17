package com.example.inventariolt.model.inventario

data class ModeloRequestDTO(
    val nombre: String,
    val marcaId: Long,
    val categoriaId: Long,
    val generoId: Long
)