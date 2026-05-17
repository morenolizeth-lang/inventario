package com.example.inventariolt.model.inventario

data class ProductoResponseDTO(
    val idProducto: Long,
    val varianteVisualId: Long,
    val modeloId: Long,
    val modeloNombre: String,
    val marcaId: Long,
    val marcaNombre: String,
    val colorPrimarioId: Long,
    val colorPrimarioNombre: String,
    val colorSecundarioId: Long?,
    val colorSecundarioNombre: String?,
    val imagen: String?,
    val tiendaId: Long,
    val tiendaNombre: String,
    val stock: Int,
    val precioCompra: Int,
    val precioVenta: Int,
    val talla: String,
    val estado: Boolean,
    val fechaRegistro: String
)