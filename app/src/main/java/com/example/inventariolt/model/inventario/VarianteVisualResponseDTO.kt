package com.example.inventariolt.model.inventario

data class VarianteVisualResponseDTO(
    val idVarianteVisual: Long,
    val modeloId: Long,
    val modeloNombre: String,
    val colorPrimarioId: Long,
    val colorPrimarioNombre: String,
    val colorSecundarioId: Long?,
    val colorSecundarioNombre: String?,
    val imagen: String?,
    val estado: Boolean
)