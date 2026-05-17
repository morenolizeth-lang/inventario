package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.model.inventario.VentaRequestDTO
import com.example.inventariolt.repository.ProductoRepository
import com.example.inventariolt.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class VentaViewModel : ViewModel() {
    private val ventaRepository = VentaRepository()
    private val productoRepository = ProductoRepository()

    private val _productosState = MutableStateFlow<VentaProductosState>(VentaProductosState.Idle)
    val productosState: StateFlow<VentaProductosState> = _productosState

    private val _ventaState = MutableStateFlow<OperacionState>(OperacionState.Idle)
    val ventaState: StateFlow<OperacionState> = _ventaState

    fun cargarProductos() {
        viewModelScope.launch {
            _productosState.value = VentaProductosState.Loading
            productoRepository.getAllProductos().onSuccess {
                _productosState.value = VentaProductosState.Success(it)
            }.onFailure {
                _productosState.value = VentaProductosState.Error(it.message ?: "Error al cargar productos")
            }
        }
    }

    fun realizarVenta(productoId: Long, usuarioId: Long, cantidad: Int, precioUnitario: Int) {
        viewModelScope.launch {
            _ventaState.value = OperacionState.Loading
            val request = VentaRequestDTO(
                fechaVenta = LocalDateTime.now(),
                cantidad = cantidad,
                precioUnitario = precioUnitario,
                total = cantidad * precioUnitario,
                productoId = productoId,
                usuarioId = usuarioId
            )
            ventaRepository.createVenta(request).onSuccess {
                _ventaState.value = OperacionState.Success("Venta realizada con éxito")
                cargarProductos() // Recargar para ver el stock actualizado
            }.onFailure {
                _ventaState.value = OperacionState.Error(it.message ?: "Error al realizar la venta")
            }
        }
    }

    fun resetVentaState() {
        _ventaState.value = OperacionState.Idle
    }
}

sealed class VentaProductosState {
    object Idle : VentaProductosState()
    object Loading : VentaProductosState()
    data class Success(val productos: List<ProductoResponseDTO>) : VentaProductosState()
    data class Error(val message: String) : VentaProductosState()
}
