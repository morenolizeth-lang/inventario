package com.example.inventariolt.model.inventario_Empleado

import com.google.gson.annotations.SerializedName

data class MarcaResponseDTO(
    @SerializedName("idMarca")
    val idMarca: Long,
    val nombre: String
)