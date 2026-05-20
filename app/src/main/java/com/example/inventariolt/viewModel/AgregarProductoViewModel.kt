package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario_Empleado.*
import com.example.inventariolt.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AgregarProductoViewModel : ViewModel() {

    private val categoriaRepository = CategoriaRepository()
    private val marcaRepository = MarcaRepository()
    private val generoRepository = GeneroRepository()
    private val colorRepository = ColorRepository()
    private val modeloRepository = ModeloRepository()
    private val varianteRepository = VarianteVisualRepository()
    private val productoRepository = ProductoRepository()
    private val _variantesConModeloState = MutableStateFlow<VariantesConModeloState>(VariantesConModeloState.Idle)
    val variantesConModeloState: StateFlow<VariantesConModeloState> = _variantesConModeloState
    // Estados
    private val _categoriasState = MutableStateFlow<CategoriasState>(CategoriasState.Idle)
    val categoriasState: StateFlow<CategoriasState> = _categoriasState

    private val _marcasState = MutableStateFlow<MarcasState>(MarcasState.Idle)
    val marcasState: StateFlow<MarcasState> = _marcasState

    private val _generosState = MutableStateFlow<GenerosState>(GenerosState.Idle)
    val generosState: StateFlow<GenerosState> = _generosState

    private val _coloresState = MutableStateFlow<ColoresState>(ColoresState.Idle)
    val coloresState: StateFlow<ColoresState> = _coloresState

    private val _modelosState = MutableStateFlow<ModelosState>(ModelosState.Idle)
    val modelosState: StateFlow<ModelosState> = _modelosState

    private val _variantesState = MutableStateFlow<VariantesState>(VariantesState.Idle)
    val variantesState: StateFlow<VariantesState> = _variantesState

    private val _crearModeloState = MutableStateFlow<CrearModeloState>(CrearModeloState.Idle)
    val crearModeloState: StateFlow<CrearModeloState> = _crearModeloState

    private val _crearVarianteState = MutableStateFlow<CrearVarianteState>(CrearVarianteState.Idle)
    val crearVarianteState: StateFlow<CrearVarianteState> = _crearVarianteState

    private val _subirImagenState = MutableStateFlow<SubirImagenState>(SubirImagenState.Idle)
    val subirImagenState: StateFlow<SubirImagenState> = _subirImagenState

    private val _crearProductoState = MutableStateFlow<CrearProductoState>(CrearProductoState.Idle)
    val crearProductoState: StateFlow<CrearProductoState> = _crearProductoState

    fun cargarDatosIniciales() {
        cargarCategorias()
        cargarMarcas()
        cargarGeneros()
        cargarColores()
        cargarVariantes()
        cargarModelos()
    }

    fun cargarCategorias() {
        viewModelScope.launch {
            _categoriasState.value = CategoriasState.Loading
            val result = categoriaRepository.getAllCategorias()
            result.onSuccess { categorias ->
                _categoriasState.value = CategoriasState.Success(categorias)
            }.onFailure { error ->
                _categoriasState.value = CategoriasState.Error(error.message ?: "Error al cargar categorías")
            }
        }
    }

    fun cargarMarcas() {
        viewModelScope.launch {
            _marcasState.value = MarcasState.Loading
            val result = marcaRepository.getAllMarcas()
            result.onSuccess { marcas ->
                _marcasState.value = MarcasState.Success(marcas)
            }.onFailure { error ->
                _marcasState.value = MarcasState.Error(error.message ?: "Error al cargar marcas")
            }
        }
    }

    fun cargarGeneros() {
        viewModelScope.launch {
            _generosState.value = GenerosState.Loading
            val result = generoRepository.getAllGeneros()
            result.onSuccess { generos ->
                _generosState.value = GenerosState.Success(generos)
            }.onFailure { error ->
                _generosState.value = GenerosState.Error(error.message ?: "Error al cargar géneros")
            }
        }
    }

    fun cargarModelos() {
        viewModelScope.launch {
            _modelosState.value = ModelosState.Loading
            val result = modeloRepository.getAllModelos()
            result.onSuccess { modelos ->
                _modelosState.value = ModelosState.Success(modelos)
            }.onFailure { error ->
                _modelosState.value = ModelosState.Error(error.message ?: "Error al cargar modelos")
            }
        }
    }

    fun cargarColores() {
        viewModelScope.launch {
            _coloresState.value = ColoresState.Loading
            val result = colorRepository.getAllColores()
            result.onSuccess { colores ->
                _coloresState.value = ColoresState.Success(colores)
            }.onFailure { error ->
                _coloresState.value = ColoresState.Error(error.message ?: "Error al cargar colores")
            }
        }
    }

    fun cargarVariantes() {
        viewModelScope.launch {
            _variantesState.value = VariantesState.Loading
            val result = varianteRepository.getAllVariantes()
            result.onSuccess { variantes ->
                _variantesState.value = VariantesState.Success(variantes)
            }.onFailure { error ->
                _variantesState.value = VariantesState.Error(error.message ?: "Error al cargar variantes")
            }
        }
    }

    fun crearModelo(nombre: String, marcaId: Long, categoriaId: Long, generoId: Long) {
        viewModelScope.launch {
            _crearModeloState.value = CrearModeloState.Loading
            val request = ModeloRequestDTO(nombre, marcaId, categoriaId, generoId)
            val result = modeloRepository.createModelo(request)
            result.onSuccess { modelo ->
                _crearModeloState.value = CrearModeloState.Success(modelo)
                cargarModelos() // Recargar la lista después de crear
            }.onFailure { error ->
                _crearModeloState.value = CrearModeloState.Error(error.message ?: "Error al crear modelo")
            }
        }
    }

    fun crearVariante(
        modeloId: Long,
        colorPrimarioId: Long,
        colorSecundarioId: Long?,
        estado: Boolean = true
    ) {
        viewModelScope.launch {
            _crearVarianteState.value = CrearVarianteState.Loading
            val request = VarianteVisualRequestDTO(modeloId, colorPrimarioId, colorSecundarioId, null, estado)
            val result = varianteRepository.createVariante(request)
            result.onSuccess { variante ->
                _crearVarianteState.value = CrearVarianteState.Success(variante)
                cargarVariantes()
            }.onFailure { error ->
                _crearVarianteState.value = CrearVarianteState.Error(error.message ?: "Error al crear variante")
            }
        }
    }

    fun subirImagenVariante(varianteId: Long, imageFile: File) {
        viewModelScope.launch {
            _subirImagenState.value = SubirImagenState.Loading
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val result = varianteRepository.uploadImagen(varianteId, body)
            result.onSuccess { variante ->
                _subirImagenState.value = SubirImagenState.Success(variante)
                cargarVariantes()
            }.onFailure { error ->
                _subirImagenState.value = SubirImagenState.Error(error.message ?: "Error al subir imagen")
            }
        }
    }

    fun crearProducto(
        varianteVisualId: Long,
        tiendaId: Long,
        stock: Int,
        precioCompra: Int,
        precioVenta: Int,
        talla: String
    ) {
        viewModelScope.launch {
            _crearProductoState.value = CrearProductoState.Loading
            val request = ProductoRequestDTO(
                varianteVisualId = varianteVisualId,
                tiendaId = tiendaId,
                stock = stock,
                precioCompra = precioCompra,
                precioVenta = precioVenta,
                talla = talla,
                estado = true
            )
            val result = productoRepository.createProducto(request)
            result.onSuccess { producto ->
                _crearProductoState.value = CrearProductoState.Success(producto)
            }.onFailure { error ->
                _crearProductoState.value = CrearProductoState.Error(error.message ?: "Error al crear producto")
            }
        }
    }

    fun resetStates() {
        _crearModeloState.value = CrearModeloState.Idle
        _crearVarianteState.value = CrearVarianteState.Idle
        _subirImagenState.value = SubirImagenState.Idle
        _crearProductoState.value = CrearProductoState.Idle
    }

    // Nuevo método: carga variantes y sus modelos asociados
    fun cargarVariantesConModelo() {
        viewModelScope.launch {
            _variantesConModeloState.value = VariantesConModeloState.Loading

            // 1. Obtener todas las variantes
            val result = varianteRepository.getAllVariantes()
            result.onSuccess { variantes ->
                // 2. Extraer IDs de modelos únicos
                val modeloIds = variantes.map { it.modeloId }.distinct()
                val modelosMap = mutableMapOf<Long, ModeloResponseDTO>()

                // 3. Cargar cada modelo (pueden ser varias peticiones, pero solo por ID único)
                for (modeloId in modeloIds) {
                    val modeloResult = modeloRepository.getModeloById(modeloId)
                    modeloResult.onSuccess { modelo ->
                        modelosMap[modeloId] = modelo
                    }
                }

                // 4. Combinar variantes con sus modelos
                val variantesEnriquecidas = variantes.map { variante ->
                    VarianteConModelo(variante, modelosMap[variante.modeloId])
                }

                _variantesConModeloState.value = VariantesConModeloState.Success(variantesEnriquecidas)
            }.onFailure { error ->
                _variantesConModeloState.value = VariantesConModeloState.Error(error.message ?: "Error al cargar variantes")
            }
        }
    }

}

// Estados
sealed class CategoriasState {
    object Idle : CategoriasState()
    object Loading : CategoriasState()
    data class Success(val categorias: List<CategoriaResponseDTO>) : CategoriasState()
    data class Error(val message: String) : CategoriasState()
}

sealed class MarcasState {
    object Idle : MarcasState()
    object Loading : MarcasState()
    data class Success(val marcas: List<MarcaResponseDTO>) : MarcasState()
    data class Error(val message: String) : MarcasState()
}

sealed class GenerosState {
    object Idle : GenerosState()
    object Loading : GenerosState()
    data class Success(val generos: List<GeneroResponseDTO>) : GenerosState()
    data class Error(val message: String) : GenerosState()
}

sealed class ColoresState {
    object Idle : ColoresState()
    object Loading : ColoresState()
    data class Success(val colores: List<ColorResponseDTO>) : ColoresState()
    data class Error(val message: String) : ColoresState()
}

sealed class ModelosState {
    object Idle : ModelosState()
    object Loading : ModelosState()
    data class Success(val modelos: List<ModeloResponseDTO>) : ModelosState()
    data class Error(val message: String) : ModelosState()
}

sealed class VariantesState {
    object Idle : VariantesState()
    object Loading : VariantesState()
    data class Success(val variantes: List<VarianteVisualResponseDTO>) : VariantesState()
    data class Error(val message: String) : VariantesState()
}

sealed class CrearModeloState {
    object Idle : CrearModeloState()
    object Loading : CrearModeloState()
    data class Success(val modelo: ModeloResponseDTO) : CrearModeloState()
    data class Error(val message: String) : CrearModeloState()
}

sealed class CrearVarianteState {
    object Idle : CrearVarianteState()
    object Loading : CrearVarianteState()
    data class Success(val variante: VarianteVisualResponseDTO) : CrearVarianteState()
    data class Error(val message: String) : CrearVarianteState()
}

sealed class SubirImagenState {
    object Idle : SubirImagenState()
    object Loading : SubirImagenState()
    data class Success(val variante: VarianteVisualResponseDTO) : SubirImagenState()
    data class Error(val message: String) : SubirImagenState()
}

sealed class CrearProductoState {
    object Idle : CrearProductoState()
    object Loading : CrearProductoState()
    data class Success(val producto: ProductoResponseDTO) : CrearProductoState()
    data class Error(val message: String) : CrearProductoState()
}

data class VarianteConModelo(
    val variante: VarianteVisualResponseDTO,
    val modelo: ModeloResponseDTO?
)

// Nuevo estado
sealed class VariantesConModeloState {
    object Idle : VariantesConModeloState()
    object Loading : VariantesConModeloState()
    data class Success(val variantes: List<VarianteConModelo>) : VariantesConModeloState()
    data class Error(val message: String) : VariantesConModeloState()
}