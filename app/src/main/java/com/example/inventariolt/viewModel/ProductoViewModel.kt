package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.model.inventario.VarianteVisualResponseDTO
import com.example.inventariolt.repository.ProductoRepository
import com.example.inventariolt.repository.VarianteVisualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()
    private val varianteRepository = VarianteVisualRepository()

    // Estados para el producto
    private val _productoState = MutableStateFlow<ProductoState>(ProductoState.Idle)
    val productoState: StateFlow<ProductoState> = _productoState

    // Estados para las variantes
    private val _variantesStates = MutableStateFlow<VariantesStates>(VariantesStates.Idle)
    val variantesStates: StateFlow<VariantesStates> = _variantesStates

    // Estado para la actualización
    private val _updateState = MutableStateFlow<UpdateProductoState>(UpdateProductoState.Idle)
    val updateState: StateFlow<UpdateProductoState> = _updateState

    // Estado para cambiar variante
    private val _cambiarVarianteState = MutableStateFlow<CambiarVarianteState>(CambiarVarianteState.Idle)
    val cambiarVarianteState: StateFlow<CambiarVarianteState> = _cambiarVarianteState

    // Cargar todas las variantes disponibles
    fun cargarVariantes() {
        viewModelScope.launch {
            _variantesStates.value = VariantesStates.Loading
            val result = varianteRepository.getAllVariantes()
            result.onSuccess { variantes ->
                _variantesStates.value = VariantesStates.Success(variantes)
            }.onFailure { error ->
                _variantesStates.value = VariantesStates.Error(error.message ?: "Error al cargar variantes")
            }
        }
    }

    // Actualizar producto
    fun actualizarProducto(
        productoId: Long,
        talla: String,
        stock: Int,
        precioCompra: Int,
        precioVenta: Int,
        varianteVisualId: Long? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateProductoState.Loading
            val result = productoRepository.updateProducto(
                productoId = productoId,
                talla = talla,
                stock = stock,
                precioCompra = precioCompra,
                precioVenta = precioVenta,
                varianteVisualId = varianteVisualId
            )
            result.onSuccess { productoActualizado ->
                _updateState.value = UpdateProductoState.Success(productoActualizado)
                // También actualizar el producto actual
                _productoState.value = ProductoState.Success(productoActualizado)
            }.onFailure { error ->
                _updateState.value = UpdateProductoState.Error(error.message ?: "Error al actualizar producto")
            }
        }
    }

    // Cambiar la variante del producto
    fun cambiarVariante(productoId: Long, nuevaVarianteId: Long) {
        viewModelScope.launch {
            _cambiarVarianteState.value = CambiarVarianteState.Loading
            val result = productoRepository.cambiarVariante(productoId, nuevaVarianteId)
            result.onSuccess { productoActualizado ->
                _cambiarVarianteState.value = CambiarVarianteState.Success(productoActualizado)
                _productoState.value = ProductoState.Success(productoActualizado)
            }.onFailure { error ->
                _cambiarVarianteState.value = CambiarVarianteState.Error(error.message ?: "Error al cambiar variante")
            }
        }
    }

    // Resetear estados
    fun resetStates() {
        _updateState.value = UpdateProductoState.Idle
        _cambiarVarianteState.value = CambiarVarianteState.Idle
    }
}

// Estados
sealed class ProductoState {
    object Idle : ProductoState()
    object Loading : ProductoState()
    data class Success(val producto: ProductoResponseDTO) : ProductoState()
    data class Error(val message: String) : ProductoState()
}

sealed class VariantesStates {
    object Idle : VariantesStates()
    object Loading : VariantesStates()
    data class Success(val variantes: List<VarianteVisualResponseDTO>) : VariantesStates()
    data class Error(val message: String) : VariantesStates()
}

sealed class UpdateProductoState {
    object Idle : UpdateProductoState()
    object Loading : UpdateProductoState()
    data class Success(val producto: ProductoResponseDTO) : UpdateProductoState()
    data class Error(val message: String) : UpdateProductoState()
}

sealed class CambiarVarianteState {
    object Idle : CambiarVarianteState()
    object Loading : CambiarVarianteState()
    data class Success(val producto: ProductoResponseDTO) : CambiarVarianteState()
    data class Error(val message: String) : CambiarVarianteState()
}