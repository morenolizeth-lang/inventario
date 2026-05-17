package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario.ProductoRequestDTO
import com.example.inventariolt.model.inventario.ProductoResponseDTO

class ProductoRepository {

    suspend fun getAllProductos(): Result<List<ProductoResponseDTO>> {
        return try {
            val response = RetrofitClient.productoApi.getAll()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProducto(request: ProductoRequestDTO): Result<ProductoResponseDTO> {
        return try {
            val response = RetrofitClient.productoApi.create(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ AGREGAR: Obtener producto por ID
    suspend fun getProductoById(productoId: Long): Result<ProductoResponseDTO> {
        return try {
            val response = RetrofitClient.productoApi.getById(productoId)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ AGREGAR: Actualizar producto completo
    suspend fun updateProducto(
        productoId: Long,
        talla: String,
        stock: Int,
        precioCompra: Int,
        precioVenta: Int,
        varianteVisualId: Long? = null
    ): Result<ProductoResponseDTO> {
        return try {
            // Obtener el producto actual primero para mantener los datos existentes
            val productoActual = getProductoById(productoId).getOrNull()

            // Crear el DTO completo con los datos actualizados
            val request = ProductoRequestDTO(
                varianteVisualId = varianteVisualId ?: (productoActual?.varianteVisualId ?: 0L),
                tiendaId = productoActual?.tiendaId ?: 0L,
                stock = stock,
                precioCompra = precioCompra,
                precioVenta = precioVenta,
                talla = talla,
                estado = productoActual?.estado ?: true
            )

            val response = RetrofitClient.productoApi.update(productoId, request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ AGREGAR: Cambiar solo la variante del producto
    suspend fun cambiarVariante(productoId: Long, nuevaVarianteId: Long): Result<ProductoResponseDTO> {
        return try {
            // Obtener el producto actual primero
            val productoActual = getProductoById(productoId).getOrNull()
                ?: return Result.failure(Exception("Producto no encontrado"))

            // Crear el DTO completo solo cambiando la variante
            val request = ProductoRequestDTO(
                varianteVisualId = nuevaVarianteId,
                tiendaId = productoActual.tiendaId,
                stock = productoActual.stock,
                precioCompra = productoActual.precioCompra,
                precioVenta = productoActual.precioVenta,
                talla = productoActual.talla,
                estado = productoActual.estado
            )

            val response = RetrofitClient.productoApi.update(productoId, request)
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Result.failure(Exception("Error ${e.code()}: ${errorBody ?: e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}