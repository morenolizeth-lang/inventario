package com.example.inventariolt.model.inventario_Empleado

import com.google.gson.annotations.SerializedName

data class ModeloResponseDTO(
    @SerializedName("idModelo")
    val idModelo: Long,
    val nombre: String,
    val marcaId: Long,
    val marcaNombre: String,
    val categoriaId: Long,
    val categoriaNombre: String,
    val generoId: Long,
    val generoNombre: String
)