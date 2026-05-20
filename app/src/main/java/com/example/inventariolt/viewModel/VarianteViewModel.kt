package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualRequestDTO
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualResponseDTO
import com.example.inventariolt.repository.VarianteVisualRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VarianteViewModel : ViewModel() {
    private val repository = VarianteVisualRepository()

    private val _variantesState = MutableStateFlow<VarianteListState>(VarianteListState.Idle)
    val variantesState: StateFlow<VarianteListState> = _variantesState

    private val _operacionState = MutableStateFlow<OperacionState>(OperacionState.Idle)
    val operacionState: StateFlow<OperacionState> = _operacionState

    fun cargarVariantes(tiendaId: Long? = null) {
        viewModelScope.launch {
            _variantesState.value = VarianteListState.Loading
            val result = if (tiendaId != null) {
                repository.getVariantesByTienda(tiendaId)
            } else {
                repository.getAllVariantes()
            }
            result.onSuccess {
                _variantesState.value = VarianteListState.Success(it)
            }.onFailure {
                _variantesState.value = VarianteListState.Error(it.message ?: "Error desconocido")
            }
        }
    }

    fun cambiarEstado(variante: VarianteVisualResponseDTO) {
        viewModelScope.launch {
            val request = VarianteVisualRequestDTO(
                modeloId = variante.modeloId,
                colorPrimarioId = variante.colorPrimarioId,
                colorSecundarioId = variante.colorSecundarioId,
                imagen = variante.imagen,
                estado = !variante.estado
            )
            repository.updateVariante(variante.idVarianteVisual, request).onSuccess {
                cargarVariantes()
            }
        }
    }

    fun actualizarVariante(id: Long, request: VarianteVisualRequestDTO) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            repository.updateVariante(id, request).onSuccess {
                _operacionState.value = OperacionState.Success("Variante actualizada")
                cargarVariantes()
            }.onFailure {
                _operacionState.value = OperacionState.Error(it.message ?: "Error al actualizar")
            }
        }
    }

    fun eliminarVariante(id: Long) {
        viewModelScope.launch {
            _operacionState.value = OperacionState.Loading
            repository.deleteVariante(id).onSuccess {
                _operacionState.value = OperacionState.Success("Variante eliminada")
                cargarVariantes()
            }.onFailure {
                _operacionState.value = OperacionState.Error(it.message ?: "Error al eliminar")
            }
        }
    }

    fun resetOperacionState() {
        _operacionState.value = OperacionState.Idle
    }
}

sealed class VarianteListState {
    object Idle : VarianteListState()
    object Loading : VarianteListState()
    data class Success(val variantes: List<VarianteVisualResponseDTO>) : VarianteListState()
    data class Error(val message: String) : VarianteListState()
}

sealed class OperacionState {
    object Idle : OperacionState()
    object Loading : OperacionState()
    data class Success(val message: String) : OperacionState()
    data class Error(val message: String) : OperacionState()
}
