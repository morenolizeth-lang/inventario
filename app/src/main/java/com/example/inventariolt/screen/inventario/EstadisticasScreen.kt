package com.example.inventariolt.screen.inventario

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.viewModel.EstadisticasData
import com.example.inventariolt.viewModel.EstadisticasState
import com.example.inventariolt.viewModel.EstadisticasViewModel
import com.example.inventariolt.viewModel.TipoGrafico
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.viewModel.PerfilState
import com.example.inventariolt.ui.theme.*
import java.time.Month
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstadisticasScreen(
    navController: NavController,
    userId: Long,
    viewModel: EstadisticasViewModel = viewModel()
) {
    val estadisticasState by viewModel.estadisticasState.collectAsState()
    val filtros by viewModel.filtrosState.collectAsState()

    val usuarioViewModel: UsuarioViewModel = viewModel()
    val perfilState by usuarioViewModel.perfilState.collectAsState()

    LaunchedEffect(userId) {
        usuarioViewModel.cargarPerfil(userId)
    }

    LaunchedEffect(perfilState) {
        if (perfilState is PerfilState.Success) {
            val tiendaId = (perfilState as PerfilState.Success).usuario.tiendaId
            viewModel.cargarDatos(tiendaId)
        }
    }

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Estadísticas",
                    subtitulo = "Análisis de rendimiento",
                    altura = 180.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (estadisticasState) {
                is EstadisticasState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AquamarinePrimary)
                }
                is EstadisticasState.Success -> {
                    val data = (estadisticasState as EstadisticasState.Success).data
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResumenCard(data)
                        AlertasCard(data)
                        GraficoCard(data, filtros.tipoGrafico)
                        ValorInventarioCard(data)
                        TopProductosCard(data)
                        DistribucionStockCard(data)
                        ProductosPorTallaCard(data)
                        TopModelosCard(data)
                    }
                }
                is EstadisticasState.Error -> {
                    Column(modifier = Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text((estadisticasState as EstadisticasState.Error).message, textAlign = TextAlign.Center)
                        Button(onClick = {
                            val state = usuarioViewModel.perfilState.value
                            if (state is PerfilState.Success) {
                                viewModel.cargarDatos(state.usuario.tiendaId)
                            } else {
                                viewModel.cargarDatos()
                            }
                        }, modifier = Modifier.padding(top = 16.dp)) { Text("Reintentar") }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ResumenCard(data: EstadisticasData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Resumen General", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatCard(value = data.totalProductos.toString(), label = "Prod.", icon = Icons.Default.Inventory, color = AquamarinePrimary)
                StatCard(value = data.totalVentas.toString(), label = "Ventas", icon = Icons.Default.ShoppingCart, color = Color(0xFF4CAF50))
                StatCard(value = data.stockTotal.toString(), label = "Stock", icon = Icons.Default.Store, color = Color(0xFF2196F3))
            }
        }
    }
}

@Composable
fun AlertasCard(data: EstadisticasData) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (data.stockCritico > 0) Color(0xFFFFEBEE) else Color(0xFFFFF3E0)),
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = if (data.stockCritico > 0) Color(0xFFD32F2F) else Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("¡Alertas de Inventario!", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    Text("Toca para ver productos con bajo stock", fontSize = 12.sp, color = Color.Gray)
                }
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                val allAlerts = (data.productosStockCritico + data.productosStockBajo).take(10)
                if (allAlerts.isNotEmpty()) {
                    Box(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allAlerts.forEach { producto ->
                                Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("${producto.modeloNombre} ${producto.marcaNombre}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Talla: ${producto.talla}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text("Stock: ${producto.stock}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (producto.stock == 0) Color.Red else Color(0xFFFF9800))
                                }
                            }
                        }
                    }
                } else {
                    Text("No hay alertas críticas", fontSize = 14.sp, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun TopProductosCard(data: EstadisticasData) {
    var expanded by remember { mutableStateOf(false) }
    if (data.productosMasVendidos.isNotEmpty()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Productos Más Vendidos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.heightIn(max = 250.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            data.productosMasVendidos.forEachIndexed { index, (producto, cantidad) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text("${index + 1}.", fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(25.dp))
                                        Column {
                                            Text("${producto.modeloNombre} ${producto.marcaNombre}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                            Text("Talla: ${producto.talla}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                    Text("$cantidad uds", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                                if (index < data.productosMasVendidos.size - 1) HorizontalDivider(thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValorInventarioCard(data: EstadisticasData) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = AquamarineLight), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = AquamarineDark) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White) }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Valor Total del Inventario", fontSize = 14.sp, color = Color.Gray)
                Text("$${String.format("%,d", data.valorInventario)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
            }
        }
    }
}

@Composable
fun DistribucionStockCard(data: EstadisticasData) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Distribución de Stock", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
            Spacer(modifier = Modifier.height(12.dp))
            StockBar("Stock Alto (>10)", data.stockAlto, data.totalProductos, Color(0xFF4CAF50))
            StockBar("Stock Normal (6-10)", data.stockNormal, data.totalProductos, Color(0xFF2196F3))
            StockBar("Stock Bajo (4-5)", data.stockBajo, data.totalProductos, Color(0xFFFF9800))
            StockBar("Stock Crítico (0-3)", data.stockCritico, data.totalProductos, Color(0xFFF44336))
        }
    }
}

@Composable
fun StockBar(label: String, value: Int, total: Int, color: Color) {
    val targetPercentage = if (total > 0) value.toFloat() / total else 0f
    val percentage by animateFloatAsState(targetValue = targetPercentage, animationSpec = tween(durationMillis = 1000))
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, color = Color.Gray)
            Text("$value productos", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(progress = { percentage }, modifier = Modifier.fillMaxWidth().height(8.dp), color = color, trackColor = Color(0xFFE0E0E0), strokeCap = StrokeCap.Round)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun GraficoCard(data: EstadisticasData, tipoGrafico: TipoGrafico) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Ventas por Mes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
            Spacer(modifier = Modifier.height(12.dp))
            if (data.ventasPorMes.isNotEmpty()) {
                when (tipoGrafico) {
                    TipoGrafico.BARRAS -> GraficoBarras(data.ventasPorMes)
                    TipoGrafico.LINEAS -> GraficoLineas(data.ventasPorMes)
                    TipoGrafico.TORTA -> GraficoTorta(data.ventasPorMes)
                }
            } else {
                Text("No hay datos de ventas", color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun GraficoBarras(ventasPorMes: Map<Month, Int>) {
    val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
    Column {
        ventasPorMes.toList().sortedBy { it.first.value }.forEach { (mes, cantidad) ->
            val targetPorcentaje = (cantidad.toFloat() / (ventasPorMes.values.maxOrNull() ?: 1))
            val animatedPorcentaje by animateFloatAsState(targetValue = targetPorcentaje, animationSpec = tween(1000))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(meses[mes.value - 1], fontSize = 12.sp, modifier = Modifier.width(40.dp))
                Box(modifier = Modifier.weight(1f).height(20.dp).clip(RoundedCornerShape(4.dp)).background(AquamarinePrimary.copy(0.1f))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animatedPorcentaje).background(AquamarineGradientHorizontal))
                }
                Text("$cantidad", fontSize = 12.sp, modifier = Modifier.width(40.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GraficoLineas(ventasPorMes: Map<Month, Int>) {
    val dataPoints = ventasPorMes.toList().sortedBy { it.first.value }.map { it.second.toFloat() }
    val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp).padding(8.dp)) {
        if (dataPoints.size > 1) {
            val spacing = size.width / (dataPoints.size - 1)
            val path = androidx.compose.ui.graphics.Path()
            dataPoints.forEachIndexed { index, value ->
                val x = index * spacing
                val y = size.height - (value / maxVal * size.height)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path = path, color = AquamarinePrimary, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
fun GraficoTorta(ventasPorMes: Map<Month, Int>) {
    val total = ventasPorMes.values.sum().toFloat().coerceAtLeast(1f)
    val colors = listOf(AquamarineDark, AquamarinePrimary, AquamarineSecondary, AquamarineTertiary)
    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var startAngle = -90f
            ventasPorMes.toList().forEachIndexed { index, (_, value) ->
                val sweepAngle = (value.toFloat() / total) * 360f
                drawArc(color = colors[index % colors.size], startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true)
                startAngle += sweepAngle
            }
        }
    }
}

@Composable
fun ProductosPorTallaCard(data: EstadisticasData) {
    var expanded by remember { mutableStateOf(false) }
    if (data.productosPorTalla.isNotEmpty()) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Productos por Talla", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            data.productosPorTalla.forEach { (talla, cantidad) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Talla $talla", fontSize = 14.sp, color = Color.Gray)
                                    Text("$cantidad productos", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AquamarinePrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopModelosCard(data: EstadisticasData) {
    var expanded by remember { mutableStateOf(false) }
    if (data.topModelos.isNotEmpty()) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }, elevation = CardDefaults.cardElevation(4.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Top Modelos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
                    Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                if (expanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                        Column {
                            data.topModelos.forEach { (modelo, cantidad) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(modelo, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                    Text("$cantidad productos", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AquamarinePrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = color.copy(0.1f)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}