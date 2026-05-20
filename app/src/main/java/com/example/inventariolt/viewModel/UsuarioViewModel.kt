package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario_Empleado.*
import com.example.inventariolt.model.login.TiendaResponseDTO
import com.example.inventariolt.model.login.UsuarioRequestDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import com.example.inventariolt.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class UsuarioViewModel : ViewModel() {
    private val usuarioRepository = UsuarioRepository()
    private val tiendaRepository = TiendaRepository()

    private val _perfilState = MutableStateFlow<PerfilState>(PerfilState.Idle)
    val perfilState: StateFlow<PerfilState> = _perfilState

    private val _updateState = MutableStateFlow<UpdatePerfilState>(UpdatePerfilState.Idle)
    val updateState: StateFlow<UpdatePerfilState> = _updateState

    private val productoRepository = ProductoRepository()

    // Inyectar o instanciar repositorios necesarios para filtros
    private val modeloRepository = ModeloRepository()
    private val categoriaRepository = CategoriaRepository()
    private val marcaRepository = MarcaRepository()
    private val generoRepository = GeneroRepository()

    private val _productosState = MutableStateFlow<ProductosState>(ProductosState.Idle)
    val productosState: StateFlow<ProductosState> = _productosState

    // Estados para catálogos (filtros)
    private val _modelosState = MutableStateFlow<List<ModeloResponseDTO>>(emptyList())
    val modelosState: StateFlow<List<ModeloResponseDTO>> = _modelosState

    private val _categoriasState = MutableStateFlow<List<CategoriaResponseDTO>>(emptyList())
    val categoriasState: StateFlow<List<CategoriaResponseDTO>> = _categoriasState

    private val _marcasState = MutableStateFlow<List<MarcaResponseDTO>>(emptyList())
    val marcasState: StateFlow<List<MarcaResponseDTO>> = _marcasState

    private val _generosState = MutableStateFlow<List<GeneroResponseDTO>>(emptyList())
    val generosState: StateFlow<List<GeneroResponseDTO>> = _generosState

    fun cargarFiltros() {
        viewModelScope.launch {
            modeloRepository.getAllModelos().onSuccess { modelos -> _modelosState.value = modelos }
            categoriaRepository.getAllCategorias().onSuccess { categorias -> _categoriasState.value = categorias }
            marcaRepository.getAllMarcas().onSuccess { marcas -> _marcasState.value = marcas }
            generoRepository.getAllGeneros().onSuccess { generos -> _generosState.value = generos }
        }
    }

    fun cargarPerfil(usuarioId: Long) {
        viewModelScope.launch {
            _perfilState.value = PerfilState.Loading
            val usuarioResult = usuarioRepository.getUsuarioById(usuarioId)
            
            usuarioResult.onSuccess { usuario ->
                if (usuario.tiendaId != null) {
                    val tiendaResult = tiendaRepository.getTiendaById(usuario.tiendaId)
                    tiendaResult.onSuccess { tienda ->
                        _perfilState.value = PerfilState.Success(usuario, tienda)
                    }.onFailure { error ->
                        _perfilState.value = PerfilState.Success(usuario, null)
                    }
                } else {
                    _perfilState.value = PerfilState.Success(usuario, null)
                }
            }.onFailure { error ->
                _perfilState.value = PerfilState.Error(error.message ?: "Error al cargar perfil")
            }
        }
    }

    fun actualizarPerfil(id: Long, request: UsuarioRequestDTO) {
        viewModelScope.launch {
            _updateState.value = UpdatePerfilState.Loading
            val result = usuarioRepository.updateUsuario(id, request)
            result.onSuccess { usuario ->
                _updateState.value = UpdatePerfilState.Success(usuario)
                // Refrescar el perfil actual
                cargarPerfil(id)
            }.onFailure { error ->
                _updateState.value = UpdatePerfilState.Error(error.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun actualizarPerfilCompleto(id: Long, request: UsuarioRequestDTO, imagePart: MultipartBody.Part?) {
        viewModelScope.launch {
            _updateState.value = UpdatePerfilState.Loading

            // 1. Actualizar información básica
            val infoResult = usuarioRepository.updateUsuario(id, request)

            infoResult.onSuccess { usuarioActualizado ->
                // 2. Si hay imagen, subirla
                if (imagePart != null) {
                    val imageResult = usuarioRepository.uploadFotoPerfil(id, imagePart)
                    imageResult.onSuccess { usuarioConFoto ->
                        _updateState.value = UpdatePerfilState.Success(usuarioConFoto)
                        cargarPerfil(id)
                    }.onFailure { error ->
                        _updateState.value = UpdatePerfilState.Error(error.message ?: "Información actualizada, pero error al subir imagen")
                        cargarPerfil(id)
                    }
                } else {
                    _updateState.value = UpdatePerfilState.Success(usuarioActualizado)
                    cargarPerfil(id)
                }
            }.onFailure { error ->
                _updateState.value = UpdatePerfilState.Error(error.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun subirFotoPerfil(id: Long, file: MultipartBody.Part) {
        viewModelScope.launch {
            _updateState.value = UpdatePerfilState.Loading
            val result = usuarioRepository.uploadFotoPerfil(id, file)
            result.onSuccess { usuario ->
                _updateState.value = UpdatePerfilState.Success(usuario)
                // Refrescar el perfil actual
                cargarPerfil(id)
            }.onFailure { error ->
                _updateState.value = UpdatePerfilState.Error(error.message ?: "Error al subir imagen")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdatePerfilState.Idle
    }

    private val _tiendasState = MutableStateFlow<TiendasState>(TiendasState.Idle)
    val tiendasState: StateFlow<TiendasState> = _tiendasState

    fun cargarTiendas() {
        viewModelScope.launch {
            _tiendasState.value = TiendasState.Loading
            val result = tiendaRepository.getAllTiendas()
            result.onSuccess { tiendas ->
                _tiendasState.value = TiendasState.Success(tiendas)
            }.onFailure { error ->
                _tiendasState.value = TiendasState.Error(error.message ?: "Error al cargar tiendas")
            }
        }
    }

    fun cargarProductos(tiendaId: Long? = null) {
        viewModelScope.launch {
            _productosState.value = ProductosState.Loading
            val result = if (tiendaId != null) {
                productoRepository.getProductosByTienda(tiendaId)
            } else {
                productoRepository.getAllProductos()
            }
            result.onSuccess { productos ->
                _productosState.value = ProductosState.Success(productos)
            }.onFailure { error ->
                val msg = error.message ?: ""
                // Manejamos solo 404 (Not Found) como lista vacía.
                // Quitamos el 500 para poder ver el error real del servidor.
                if (msg.contains("404")) {
                    _productosState.value = ProductosState.Success(emptyList())
                } else {
                    _productosState.value = ProductosState.Error(if (msg.isEmpty()) "Error al cargar productos" else msg)
                }
            }
        }
    }
}

sealed class ProductosState {
    object Idle : ProductosState()
    object Loading : ProductosState()
    data class Success(val productos: List<ProductoResponseDTO>) : ProductosState()
    data class Error(val message: String) : ProductosState()
}

sealed class PerfilState {
    object Idle : PerfilState()
    object Loading : PerfilState()
    data class Success(val usuario: UsuarioResponseDTO, val tienda: TiendaResponseDTO?) : PerfilState()
    data class Error(val message: String) : PerfilState()
}

sealed class UpdatePerfilState {
    object Idle : UpdatePerfilState()
    object Loading : UpdatePerfilState()
    data class Success(val usuario: UsuarioResponseDTO) : UpdatePerfilState()
    data class Error(val message: String) : UpdatePerfilState()
}

sealed class TiendasState {
    object Idle : TiendasState()
    object Loading : TiendasState()
    data class Success(val tiendas: List<TiendaResponseDTO>) : TiendasState()
    data class Error(val message: String) : TiendasState()
}
