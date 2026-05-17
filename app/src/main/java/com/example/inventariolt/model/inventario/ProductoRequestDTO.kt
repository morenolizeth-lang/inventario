package com.example.inventariolt.model.inventario

data class ProductoRequestDTO(
    val varianteVisualId: Long,
    val tiendaId: Long,
    val stock: Int,
    val precioCompra: Int,
    val precioVenta: Int,
    val talla: String,
    val estado: Boolean
)