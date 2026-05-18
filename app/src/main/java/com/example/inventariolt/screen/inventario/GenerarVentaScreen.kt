package com.example.inventariolt.screen.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.viewModel.OperacionState
import com.example.inventariolt.viewModel.VentaProductosState
import com.example.inventariolt.viewModel.VentaViewModel
import com.example.inventariolt.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerarVentaScreen(
    navController: NavController,
    userId: Long,
    viewModel: VentaViewModel = viewModel()
) {
    val productosState by viewModel.productosState.collectAsState()
    val ventaState by viewModel.ventaState.collectAsState()

    var selectedProducto by remember { mutableStateOf<ProductoResponseDTO?>(null) }
    var cantidad by remember { mutableStateOf("1") }
    var expanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }

    LaunchedEffect(ventaState) {
        if (ventaState is OperacionState.Success) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Venta Realizada", fontWeight = FontWeight.Bold) },
            text = { Text("La venta se ha procesado correctamente y el stock ha sido actualizado.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetVentaState()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary)
                ) { Text("Aceptar") }
            }
        )
    }

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Generar Venta",
                    subtitulo = "Registrar nueva transacción",
                    altura = 180.dp
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar Producto", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarineDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarineDark)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedProducto != null) "${selectedProducto!!.modeloNombre} ${selectedProducto!!.marcaNombre} - T. ${selectedProducto!!.talla}" else "Seleccione un producto",
                                fontSize = 14.sp,
                                color = if (selectedProducto != null) AquamarineDark else Color.Gray
                            )
                            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                        }
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        when (productosState) {
                            is VentaProductosState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                            is VentaProductosState.Success -> {
                                val productos = (productosState as VentaProductosState.Success).productos.filter { it.stock > 0 }
                                Box(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        productos.forEach { producto ->
                                            ProductoCard(
                                                producto = producto,
                                                isSelected = selectedProducto?.idProducto == producto.idProducto,
                                                onClick = { selectedProducto = producto; expanded = false }
                                            )
                                        }
                                    }
                                }
                            }
                            else -> Text("No se pudieron cargar los productos", color = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedProducto != null) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AquamarineLight), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Stock disponible", fontSize = 12.sp, color = Color.Gray)
                            Text("${selectedProducto!!.stock} uds", fontWeight = FontWeight.Bold, color = if (selectedProducto!!.stock <= 5) Color.Red else AquamarineDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Precio", fontSize = 12.sp, color = Color.Gray)
                            Text("$${String.format("%,d", selectedProducto!!.precioVenta)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = cantidad,
                onValueChange = { if (it.all { c -> c.isDigit() }) cantidad = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquamarinePrimary,
                    focusedLabelColor = AquamarinePrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            val total = (cantidad.toIntOrNull() ?: 0) * (selectedProducto?.precioVenta ?: 0)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AquamarineDark), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("TOTAL", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%,d", total)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    selectedProducto?.let {
                        viewModel.realizarVenta(it.idProducto, userId, cantidad.toIntOrNull() ?: 0, it.precioVenta)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = selectedProducto != null && (cantidad.toIntOrNull() ?: 0) > 0 && (cantidad.toIntOrNull() ?: 0) <= (selectedProducto?.stock ?: 0) && ventaState !is OperacionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary)
            ) {
                if (ventaState is OperacionState.Loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Confirmar Venta", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProductoCard(producto: ProductoResponseDTO, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) AquamarineLight else Color(0xFFF5F5F5)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)), color = Color.LightGray) {
                if (producto.imagen != null) {
                    SubcomposeAsyncImage(model = producto.imagen, contentDescription = null, contentScale = ContentScale.Crop)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${producto.modeloNombre} ${producto.marcaNombre}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Text("Talla: ${producto.talla} | Stock: ${producto.stock}", fontSize = 11.sp, color = Color.Gray)
            }
            Text("$${String.format("%,d", producto.precioVenta)}", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
        }
    }
}