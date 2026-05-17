package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.login.TiendaResponseDTO
import com.example.inventariolt.repository.TiendaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TiendaViewModel : ViewModel() {

    private val repository = TiendaRepository()

    // Estados
    private val _createState = MutableStateFlow<CreateTiendaState>(CreateTiendaState.Idle)
    val createState: StateFlow<CreateTiendaState> = _createState

    private val _getAllState = MutableStateFlow<GetAllTiendasState>(GetAllTiendasState.Idle)
    val getAllState: StateFlow<GetAllTiendasState> = _getAllState

    private val _getByIdState = MutableStateFlow<GetTiendaByIdState>(GetTiendaByIdState.Idle)
    val getByIdState: StateFlow<GetTiendaByIdState> = _getByIdState

    private val _updateState = MutableStateFlow<UpdateTiendaState>(UpdateTiendaState.Idle)
    val updateState: StateFlow<UpdateTiendaState> = _updateState

    private val _deleteState = MutableStateFlow<DeleteTiendaState>(DeleteTiendaState.Idle)
    val deleteState: StateFlow<DeleteTiendaState> = _deleteState

    // Crear tienda
    fun createTienda(nombre: String, direccion: String, telefono: String) {
        viewModelScope.launch {
            _createState.value = CreateTiendaState.Loading
            val result = repository.createTienda(nombre, direccion, telefono)
            result.onSuccess { tienda ->
                _createState.value = CreateTiendaState.Success(tienda)
            }.onFailure { error ->
                _createState.value = CreateTiendaState.Error(error.message ?: "Error al crear tienda")
            }
        }
    }

    // Obtener todas las tiendas - CORREGIDO (no debe ser suspend fun)
    fun getAllTiendas() {
        viewModelScope.launch {
            println("🔍 DEBUG ViewModel: Iniciando getAllTiendas()")
            _getAllState.value = GetAllTiendasState.Loading
            val result = repository.getAllTiendas()
            result.onSuccess { tiendas ->
                println("✅ ViewModel: ${tiendas.size} tiendas cargadas correctamente")
                _getAllState.value = GetAllTiendasState.Success(tiendas)
            }.onFailure { error ->
                println("❌ ViewModel Error: ${error.message}")
                _getAllState.value = GetAllTiendasState.Error(error.message ?: "Error al obtener tiendas")
            }
        }
    }

    // Obtener tienda por ID
    fun getTiendaById(id: Long) {
        viewModelScope.launch {
            _getByIdState.value = GetTiendaByIdState.Loading
            val result = repository.getTiendaById(id)
            result.onSuccess { tienda ->
                _getByIdState.value = GetTiendaByIdState.Success(tienda)
            }.onFailure { error ->
                _getByIdState.value = GetTiendaByIdState.Error(error.message ?: "Error al obtener tienda")
            }
        }
    }

    // Actualizar tienda
    fun updateTienda(id: Long, nombre: String, direccion: String, telefono: String) {
        viewModelScope.launch {
            _updateState.value = UpdateTiendaState.Loading
            val result = repository.updateTienda(id, nombre, direccion, telefono)
            result.onSuccess { tienda ->
                _updateState.value = UpdateTiendaState.Success(tienda)
            }.onFailure { error ->
                _updateState.value = UpdateTiendaState.Error(error.message ?: "Error al actualizar tienda")
            }
        }
    }

    // Eliminar tienda
    fun deleteTienda(id: Long) {
        viewModelScope.launch {
            _deleteState.value = DeleteTiendaState.Loading
            val result = repository.deleteTienda(id)
            result.onSuccess {
                _deleteState.value = DeleteTiendaState.Success
            }.onFailure { error ->
                _deleteState.value = DeleteTiendaState.Error(error.message ?: "Error al eliminar tienda")
            }
        }
    }

    // Resetear estados
    fun resetStates() {
        _createState.value = CreateTiendaState.Idle
        _getAllState.value = GetAllTiendasState.Idle
        _getByIdState.value = GetTiendaByIdState.Idle
        _updateState.value = UpdateTiendaState.Idle
        _deleteState.value = DeleteTiendaState.Idle
    }
}

// Estados para cada operación
sealed class CreateTiendaState {
    object Idle : CreateTiendaState()
    object Loading : CreateTiendaState()
    data class Success(val tienda: TiendaResponseDTO) : CreateTiendaState()
    data class Error(val message: String) : CreateTiendaState()
}

sealed class GetAllTiendasState {
    object Idle : GetAllTiendasState()
    object Loading : GetAllTiendasState()
    data class Success(val tiendas: List<TiendaResponseDTO>) : GetAllTiendasState()
    data class Error(val message: String) : GetAllTiendasState()
}

sealed class GetTiendaByIdState {
    object Idle : GetTiendaByIdState()
    object Loading : GetTiendaByIdState()
    data class Success(val tienda: TiendaResponseDTO) : GetTiendaByIdState()
    data class Error(val message: String) : GetTiendaByIdState()
}

sealed class UpdateTiendaState {
    object Idle : UpdateTiendaState()
    object Loading : UpdateTiendaState()
    data class Success(val tienda: TiendaResponseDTO) : UpdateTiendaState()
    data class Error(val message: String) : UpdateTiendaState()
}

sealed class DeleteTiendaState {
    object Idle : DeleteTiendaState()
    object Loading : DeleteTiendaState()
    object Success : DeleteTiendaState()
    data class Error(val message: String) : DeleteTiendaState()
}