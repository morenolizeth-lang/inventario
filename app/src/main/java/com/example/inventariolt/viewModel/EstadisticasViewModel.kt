package com.example.inventariolt.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.model.inventario.VentaResponseDTO
import com.example.inventariolt.repository.ProductoRepository
import com.example.inventariolt.repository.VentaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.Month

class EstadisticasViewModel : ViewModel() {

    private val productoRepository = ProductoRepository()
    private val ventaRepository = VentaRepository()

    private val _productosState = MutableStateFlow<EProductosState>(EProductosState.Idle)
    private val _ventasState = MutableStateFlow<EVentasState>(EVentasState.Idle)

    private val _filtrosState = MutableStateFlow(FiltrosState(
        fechaInicio = LocalDateTime.now().minusMonths(1),
        fechaFin = LocalDateTime.now(),
        tipoGrafico = TipoGrafico.BARRAS
    ))
    val filtrosState: StateFlow<FiltrosState> = _filtrosState

    val estadisticasState: StateFlow<EstadisticasState> = combine(
        _productosState, _ventasState, _filtrosState
    ) { pState, vState, filtros ->
        when {
            pState is EProductosState.Loading || vState is EVentasState.Loading -> {
                EstadisticasState.Loading
            }
            pState is EProductosState.Success && vState is EVentasState.Success -> {
                try {
                    val data = calcularEstadisticas(pState.productos, vState.ventas, filtros)
                    EstadisticasState.Success(data)
                } catch (e: Exception) {
                    EstadisticasState.Error(e.message ?: "Error al calcular estadísticas")
                }
            }
            pState is EProductosState.Error -> {
                EstadisticasState.Error(pState.message)
            }
            vState is EVentasState.Error -> {
                EstadisticasState.Error(vState.message)
            }
            else -> {
                EstadisticasState.Idle
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EstadisticasState.Idle
    )

    fun actualizarFiltros(fechaInicio: LocalDateTime? = null, fechaFin: LocalDateTime? = null, tipoGrafico: TipoGrafico? = null) {
        val current = _filtrosState.value
        _filtrosState.value = FiltrosState(
            fechaInicio = fechaInicio ?: current.fechaInicio,
            fechaFin = fechaFin ?: current.fechaFin,
            tipoGrafico = tipoGrafico ?: current.tipoGrafico
        )
    }

    fun cargarDatos(tiendaId: Long? = null) {
        viewModelScope.launch {
            _productosState.value = EProductosState.Loading
            _ventasState.value = EVentasState.Loading

            val productosResult = if (tiendaId != null) {
                productoRepository.getProductosByTienda(tiendaId)
            } else {
                productoRepository.getAllProductos()
            }

            val ventasResult = if (tiendaId != null) {
                ventaRepository.getVentasByTienda(tiendaId)
            } else {
                ventaRepository.getAllVentas()
            }

            productosResult.onSuccess { productos ->
                _productosState.value = EProductosState.Success(productos)
            }.onFailure { error ->
                if (error.message?.contains("404") == true || error.message?.contains("500") == true) {
                    _productosState.value = EProductosState.Success(emptyList())
                } else {
                    _productosState.value = EProductosState.Error(error.message ?: "Error al cargar productos")
                }
            }

            ventasResult.onSuccess { ventas ->
                _ventasState.value = EVentasState.Success(ventas)
            }.onFailure { error ->
                if (error.message?.contains("404") == true || error.message?.contains("500") == true) {
                    _ventasState.value = EVentasState.Success(emptyList())
                } else {
                    _ventasState.value = EVentasState.Error(error.message ?: "Error al cargar ventas")
                }
            }
        }
    }

    private fun calcularEstadisticas(productos: List<ProductoResponseDTO>, ventas: List<VentaResponseDTO>, filtros: FiltrosState): EstadisticasData {
        val productIdsTienda = productos.map { it.idProducto }.toSet()
        
        val ventasFiltradas = ventas.filter { venta ->
            venta.fechaVenta.isAfter(filtros.fechaInicio) && 
            venta.fechaVenta.isBefore(filtros.fechaFin) &&
            (productIdsTienda.isEmpty() || venta.productoId in productIdsTienda)
        }

        val totalProductos = productos.size
        val totalVentas = ventasFiltradas.size
        val totalIngresos = ventasFiltradas.sumOf { it.total }
        val stockTotal = productos.sumOf { it.stock }
        val valorInventario = productos.sumOf { it.precioCompra.toLong() * it.stock }

        val stockCritico = productos.filter { it.stock in 0..3 }.size
        val stockBajo = productos.filter { it.stock in 4..5 }.size
        val stockNormal = productos.filter { it.stock in 6..10 }.size
        val stockAlto = productos.filter { it.stock > 10 }.size

        val productosStockCritico = productos.filter { it.stock in 0..3 }
        val productosStockBajo = productos.filter { it.stock in 4..5 }

        val productosMasVendidos = ventasFiltradas
            .groupBy { it.productoId }
            .mapValues { it.value.sumOf { v -> v.cantidad } }
            .toList()
            .sortedByDescending { it.second }
            .take(10)
            .mapNotNull { (id, cant) ->
                productos.find { it.idProducto == id }?.let { it to cant }
            }

        val ventasPorMes = ventasFiltradas
            .groupBy { it.fechaVenta.month }
            .mapValues { it.value.size }

        val ultimos30Dias = (0..30).associate { days ->
            val fecha = LocalDateTime.now().minusDays(days.toLong())
            fecha.toLocalDate().toString() to ventasFiltradas.count {
                it.fechaVenta.toLocalDate() == fecha.toLocalDate()
            }
        }

        val topModelos = productos
            .groupBy { it.modeloNombre }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val productosPorTalla = productos
            .groupBy { it.talla }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.first }

        return EstadisticasData(
            totalProductos = totalProductos,
            totalVentas = totalVentas,
            totalIngresos = totalIngresos,
            stockTotal = stockTotal,
            valorInventario = valorInventario,
            stockBajo = stockBajo,
            stockCritico = stockCritico,
            stockNormal = stockNormal,
            stockAlto = stockAlto,
            productosMasVendidos = productosMasVendidos,
            productosStockBajo = productosStockBajo,
            productosStockCritico = productosStockCritico,
            ventasPorMes = ventasPorMes,
            ultimos30Dias = ultimos30Dias,
            topModelos = topModelos,
            productosPorTalla = productosPorTalla
        )
    }
}

data class EstadisticasData(
    val totalProductos: Int,
    val totalVentas: Int,
    val totalIngresos: Int,
    val stockTotal: Int,
    val valorInventario: Long,
    val stockBajo: Int,
    val stockCritico: Int,
    val stockNormal: Int,
    val stockAlto: Int,
    val productosMasVendidos: List<Pair<ProductoResponseDTO, Int>>,
    val productosStockBajo: List<ProductoResponseDTO>,
    val productosStockCritico: List<ProductoResponseDTO>,
    val ventasPorMes: Map<Month, Int>,
    val ultimos30Dias: Map<String, Int>,
    val topModelos: List<Pair<String, Int>>,
    val productosPorTalla: List<Pair<String, Int>>
)

data class FiltrosState(
    val fechaInicio: LocalDateTime,
    val fechaFin: LocalDateTime,
    val tipoGrafico: TipoGrafico
)

enum class TipoGrafico {
    BARRAS, LINEAS, TORTA
}

sealed class EProductosState {
    object Idle : EProductosState()
    object Loading : EProductosState()
    data class Success(val productos: List<ProductoResponseDTO>) : EProductosState()
    data class Error(val message: String) : EProductosState()
}

sealed class EVentasState {
    object Idle : EVentasState()
    object Loading : EVentasState()
    data class Success(val ventas: List<VentaResponseDTO>) : EVentasState()
    data class Error(val message: String) : EVentasState()
}

sealed class EstadisticasState {
    object Idle : EstadisticasState()
    object Loading : EstadisticasState()
    data class Success(val data: EstadisticasData) : EstadisticasState()
    data class Error(val message: String) : EstadisticasState()
}
