package com.example.inventariolt.model.inventario_Empleado

data class ModeloRequestDTO(
    val nombre: String,
    val marcaId: Long,
    val categoriaId: Long,
    val generoId: Long
)