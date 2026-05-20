package com.example.inventariolt.model.inventario_Empleado

data class VarianteVisualRequestDTO(
    val modeloId: Long,
    val colorPrimarioId: Long,
    val colorSecundarioId: Long?,
    val imagen: String?,
    val estado: Boolean
)