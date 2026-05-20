package com.example.inventariolt.repository

import com.example.inventariolt.interfaces.RetrofitClient
import com.example.inventariolt.model.inventario_Empleado.ProductoRequestDTO
import com.example.inventariolt.model.inventario_Empleado.ProductoResponseDTO

class ProductoRepository {

    suspend fun getAllProductos(): Result<List<ProductoResponseDTO>> {
        return try {
            val response = RetrofitClient.productoApi.getAll()
            Result.success(response)
        } catch (e: retrofit2.HttpException) {
            Result.failure(Exception("Error ${e.code()}: ${e.message()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductosByTienda(tiendaId: Long): Result<List<ProductoResponseDTO>> {
        return try {
            // El backend no tiene /tienda/{id}, filtramos localmente o usamos getAll si es necesario
            val response = RetrofitClient.productoApi.getAll()
            val filtrados = response.filter { it.tiendaId == tiendaId }
            Result.success(filtrados)
        } catch (e: retrofit2.HttpException) {
            Result.failure(Exception("Error ${e.code()}: ${e.message()}"))
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

    // ✅ AGREGAR: Eliminar producto
    suspend fun deleteProducto(productoId: Long): Result<Unit> {
        return try {
            val response = RetrofitClient.productoApi.delete(productoId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("Error ${response.code()}: ${errorBody ?: "Error al eliminar el producto"}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ AGREGAR: Cambiar solo la variante
    suspend fun cambiarVariante(productoId: Long, nuevaVarianteId: Long): Result<ProductoResponseDTO> {
        return try {
            val productoActual = getProductoById(productoId).getOrNull()
                ?: return Result.failure(Exception("No se encontró el producto para cambiar la variante"))

            // Retornamos explícitamente el resultado de la actualización
            return updateProducto(
                productoId = productoId,
                talla = productoActual.talla,
                stock = productoActual.stock,
                precioCompra = productoActual.precioCompra,
                precioVenta = productoActual.precioVenta,
                varianteVisualId = nuevaVarianteId
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
