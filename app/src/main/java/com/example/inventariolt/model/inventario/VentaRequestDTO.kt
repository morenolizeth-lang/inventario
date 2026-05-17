package com.example.inventariolt.model.inventario

import java.time.LocalDateTime

data class VentaRequestDTO(
    val fechaVenta: LocalDateTime,
    val cantidad: Int,
    val precioUnitario: Int?,      // Opcional para el frontend
    val total: Int?,                // Opcional para el frontend
    val productoId: Long,
    val usuarioId: Long
)