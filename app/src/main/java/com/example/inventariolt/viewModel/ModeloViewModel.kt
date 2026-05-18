package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario.ModeloRequestDTO
import com.example.inventariolt.model.inventario.ModeloResponseDTO
import com.example.inventariolt.repository.ModeloRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModeloViewModel : ViewModel() {

    // Estado para detalle de modelo
    private val _modeloDetalleState = MutableStateFlow<ModeloDetalleState>(ModeloDetalleState.Idle)
    val modeloDetalleState: StateFlow<ModeloDetalleState> = _modeloDetalleState.asStateFlow()

    private val repository = ModeloRepository()

    // Estado para lista de modelos
    private val _modelosState = MutableStateFlow<ModeloListState>(ModeloListState.Idle)
    val modelosState: StateFlow<ModeloListState> = _modelosState.asStateFlow()

    // Estado para operación de crear
    private val _crearModeloState = MutableStateFlow<CrearModeloState>(CrearModeloState.Idle)
    val crearModeloState: StateFlow<CrearModeloState> = _crearModeloState.asStateFlow()

    // Estado para operación de actualizar
    private val _actualizarModeloState = MutableStateFlow<ActualizarModeloState>(ActualizarModeloState.Idle)
    val actualizarModeloState: StateFlow<ActualizarModeloState> = _actualizarModeloState.asStateFlow()

    // Estado para operación de eliminar
    private val _eliminarModeloState = MutableStateFlow<EliminarModeloState>(EliminarModeloState.Idle)
    val eliminarModeloState: StateFlow<EliminarModeloState> = _eliminarModeloState.asStateFlow()

    // ========== OPERACIONES CRUD ==========

    // Obtener todos los modelos
    fun cargarModelos(tiendaId: Long? = null) {
        viewModelScope.launch {
            _modelosState.value = ModeloListState.Loading
            val result = if (tiendaId != null) {
                repository.getModelosByTienda(tiendaId)
            } else {
                repository.getAllModelos()
            }
            result.onSuccess { modelos ->
                _modelosState.value = ModeloListState.Success(modelos)
            }.onFailure { error ->
                _modelosState.value = ModeloListState.Error(error.message ?: "Error al cargar modelos")
            }
        }
    }

    // Obtener modelo por ID
    fun cargarModeloPorId(id: Long) {
        viewModelScope.launch {
            _modeloDetalleState.value = ModeloDetalleState.Loading
            val result = repository.getModeloById(id)
            result.onSuccess { modelo ->
                _modeloDetalleState.value = ModeloDetalleState.Success(modelo)
            }.onFailure { error ->
                _modeloDetalleState.value = ModeloDetalleState.Error(error.message ?: "Error al cargar el modelo")
            }
        }
    }

    // Actualizar modelo existente
    fun actualizarModelo(id: Long, nombre: String, marcaId: Long, categoriaId: Long, generoId: Long) {
        viewModelScope.launch {
            _actualizarModeloState.value = ActualizarModeloState.Loading
            val request = ModeloRequestDTO(nombre, marcaId, categoriaId, generoId)
            val result = repository.updateModelo(id, request)
            result.onSuccess { modelo ->
                _actualizarModeloState.value = ActualizarModeloState.Success(modelo)
                cargarModelos() // Recargar la lista después de actualizar
            }.onFailure { error ->
                _actualizarModeloState.value = ActualizarModeloState.Error(error.message ?: "Error al actualizar modelo")
            }
        }
    }

    // Eliminar modelo
    fun eliminarModelo(id: Long) {
        viewModelScope.launch {
            _eliminarModeloState.value = EliminarModeloState.Loading
            val result = repository.deleteModelo(id)
            result.onSuccess {
                _eliminarModeloState.value = EliminarModeloState.Success("Modelo eliminado exitosamente")
                cargarModelos() // Recargar la lista después de eliminar
            }.onFailure { error ->
                _eliminarModeloState.value = EliminarModeloState.Error(error.message ?: "Error al eliminar modelo")
            }
        }
    }

    // ========== RESET DE ESTADOS ==========

    fun resetCrearModeloState() {
        _crearModeloState.value = CrearModeloState.Idle
    }

    fun resetActualizarModeloState() {
        _actualizarModeloState.value = ActualizarModeloState.Idle
    }

    fun resetEliminarModeloState() {
        _eliminarModeloState.value = EliminarModeloState.Idle
    }

    fun resetAllStates() {
        resetCrearModeloState()
        resetActualizarModeloState()
        resetEliminarModeloState()
    }
}

// Estado para la lista de modelos
sealed class ModeloListState {
    object Idle : ModeloListState()
    object Loading : ModeloListState()
    data class Success(val modelos: List<ModeloResponseDTO>) : ModeloListState()
    data class Error(val message: String) : ModeloListState()
}

// Estado para actualizar modelo
sealed class ActualizarModeloState {
    object Idle : ActualizarModeloState()
    object Loading : ActualizarModeloState()
    data class Success(val modelo: ModeloResponseDTO) : ActualizarModeloState()
    data class Error(val message: String) : ActualizarModeloState()
}

// Estado para eliminar modelo
sealed class EliminarModeloState {
    object Idle : EliminarModeloState()
    object Loading : EliminarModeloState()
    data class Success(val message: String) : EliminarModeloState()
    data class Error(val message: String) : EliminarModeloState()
}
sealed class ModeloDetalleState {
    object Idle : ModeloDetalleState()
    object Loading : ModeloDetalleState()
    data class Success(val modelo: ModeloResponseDTO) : ModeloDetalleState()
    data class Error(val message: String) : ModeloDetalleState()
}