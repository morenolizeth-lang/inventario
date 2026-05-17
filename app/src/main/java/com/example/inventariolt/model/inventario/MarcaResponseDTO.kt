package com.example.inventariolt.model.inventario

import com.google.gson.annotations.SerializedName

data class MarcaResponseDTO(
    @SerializedName("idMarca")
    val idMarca: Long,
    val nombre: String
)