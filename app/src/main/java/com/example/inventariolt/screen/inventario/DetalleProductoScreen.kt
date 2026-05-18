package com.example.inventariolt.screen.inventario

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.model.inventario.ProductoResponseDTO
import com.example.inventariolt.model.inventario.VarianteVisualResponseDTO
import com.example.inventariolt.viewModel.CambiarVarianteState
import com.example.inventariolt.viewModel.ModeloState
import com.example.inventariolt.viewModel.ProductoViewModel
import com.example.inventariolt.viewModel.UpdateProductoState
import com.example.inventariolt.viewModel.VariantesStates
import com.example.inventariolt.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    navController: NavController,
    producto: ProductoResponseDTO,
    userId: Long,
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isEditing by remember { mutableStateOf(false) }
    var showVarianteSelector by remember { mutableStateOf(false) }

    // Estados para edición
    var editStock by remember { mutableStateOf(producto.stock.toString()) }
    var editPrecioCompra by remember { mutableStateOf(producto.precioCompra.toString()) }
    var editPrecioVenta by remember { mutableStateOf(producto.precioVenta.toString()) }
    var editTalla by remember { mutableStateOf(producto.talla) }
    var varianteSeleccionada by remember { mutableStateOf<VarianteVisualResponseDTO?>(null) }

    // Estados del ViewModel
    val variantesStates by viewModel.variantesStates.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val cambiarVarianteState by viewModel.cambiarVarianteState.collectAsState()
    val modeloState by viewModel.modeloState.collectAsState()

    // Cargar variantes y el modelo
    LaunchedEffect(Unit) {
        viewModel.cargarVariantes()
        viewModel.cargarModeloPorId(producto.modeloId)
    }

    // Manejar resultado de actualización
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateProductoState.Success -> {
                Toast.makeText(context, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                isEditing = false
                viewModel.resetStates()
                val productoActualizado = (updateState as UpdateProductoState.Success).producto
                editStock = productoActualizado.stock.toString()
                editPrecioCompra = productoActualizado.precioCompra.toString()
                editPrecioVenta = productoActualizado.precioVenta.toString()
                editTalla = productoActualizado.talla
            }
            is UpdateProductoState.Error -> {
                Toast.makeText(context, (updateState as UpdateProductoState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetStates()
            }
            else -> {}
        }
    }

    // Manejar resultado de cambio de variante
    LaunchedEffect(cambiarVarianteState) {
        when (cambiarVarianteState) {
            is CambiarVarianteState.Success -> {
                Toast.makeText(context, "Variante cambiada exitosamente", Toast.LENGTH_SHORT).show()
                showVarianteSelector = false
                viewModel.resetStates()
            }
            is CambiarVarianteState.Error -> {
                Toast.makeText(context, (cambiarVarianteState as CambiarVarianteState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetStates()
            }
            else -> {}
        }
    }

    fun saveChanges() {
        val stockInt = editStock.toIntOrNull()
        val precioCompraInt = editPrecioCompra.toIntOrNull()
        val precioVentaInt = editPrecioVenta.toIntOrNull()

        if (stockInt == null) {
            Toast.makeText(context, "Stock inválido", Toast.LENGTH_SHORT).show()
            return
        }
        if (precioCompraInt == null) {
            Toast.makeText(context, "Precio de compra inválido", Toast.LENGTH_SHORT).show()
            return
        }
        if (precioVentaInt == null) {
            Toast.makeText(context, "Precio de venta inválido", Toast.LENGTH_SHORT).show()
            return
        }
        if (editTalla.isBlank()) {
            Toast.makeText(context, "Talla inválida", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.actualizarProducto(
            productoId = producto.idProducto,
            talla = editTalla,
            stock = stockInt,
            precioCompra = precioCompraInt,
            precioVenta = precioVentaInt
        )
    }

    fun cambiarVariante(nuevaVarianteId: Long) {
        viewModel.cambiarVariante(producto.idProducto, nuevaVarianteId)
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            // Header con Imagen cuadrada
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AquamarineDark, AquamarinePrimary)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                    IconButton(onClick = { isEditing = !isEditing }) {
                        Icon(
                            if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Cancelar edición" else "Editar",
                            tint = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        SubcomposeAsyncImage(
                            model = producto.imagen ?: varianteSeleccionada?.imagen,
                            contentDescription = producto.modeloNombre,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFF0F0F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre del producto
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = producto.modeloNombre,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquamarineDark,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AquamarinePrimary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = producto.colorPrimarioNombre,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AquamarineDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            if (producto.colorSecundarioNombre != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = AquamarinePrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = producto.colorSecundarioNombre,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AquamarineDark,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Botón cambiar variante (solo edición)
                if (isEditing) {
                    Button(
                        onClick = { showVarianteSelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambiar Variante Visual")
                    }
                }

                // Información del Producto
                Text(
                    "Información del Producto",
                    style = MaterialTheme.typography.titleMedium,
                    color = AquamarinePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Talla
                InfoCardEditable(
                    icon = Icons.Default.Straighten,
                    label = "Talla",
                    value = editTalla,
                    isEditing = isEditing,
                    onValueChange = { editTalla = it }
                )

                // Stock
                InfoCardEditable(
                    icon = Icons.Default.Inventory,
                    label = "Stock disponible",
                    value = editStock,
                    isEditing = isEditing,
                    isNumeric = true,
                    onValueChange = { editStock = it }
                )

                // Precio Venta
                InfoCardEditable(
                    icon = Icons.Default.ShoppingCart,
                    label = "Precio Venta",
                    value = editPrecioVenta,
                    isEditing = isEditing,
                    isNumeric = true,
                    onValueChange = { editPrecioVenta = it },
                    isPrecio = true
                )

                // Precio Compra
                InfoCardEditable(
                    icon = Icons.Default.AttachMoney,
                    label = "Precio Compra",
                    value = editPrecioCompra,
                    isEditing = isEditing,
                    isNumeric = true,
                    onValueChange = { editPrecioCompra = it },
                    isPrecio = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Información del Modelo
                Text(
                    "Información del Modelo",
                    style = MaterialTheme.typography.titleMedium,
                    color = AquamarinePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentModeloState = modeloState

                when (currentModeloState) {
                    is ModeloState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    is ModeloState.Success -> {
                        val modelo = currentModeloState.modelo
                        InfoCard(
                            icon = Icons.Default.Sell,
                            label = "Marca",
                            value = modelo.marcaNombre
                        )
                        InfoCard(
                            icon = Icons.Default.Category,
                            label = "Categoría",
                            value = modelo.categoriaNombre
                        )
                        InfoCard(
                            icon = Icons.Default.Wc,
                            label = "Género",
                            value = modelo.generoNombre
                        )
                    }
                    is ModeloState.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = currentModeloState.message,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tienda
                Text(
                    "Información de la Tienda",
                    style = MaterialTheme.typography.titleMedium,
                    color = AquamarinePrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (producto.tiendaId != null) {
                    InfoCard(
                        icon = Icons.Default.Store,
                        label = "Tienda",
                        value = producto.tiendaNombre
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AquamarineLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AquamarineDark)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Este producto no está asignado a ninguna tienda", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fecha de registro
                Text(
                    text = "Registrado el ${producto.fechaRegistro}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Botón Guardar (solo visible en modo edición)
                if (isEditing) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { saveChanges() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                        enabled = updateState !is UpdateProductoState.Loading
                    ) {
                        if (updateState is UpdateProductoState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("GUARDAR CAMBIOS", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Diálogo para seleccionar nueva variante
    if (showVarianteSelector) {
        AlertDialog(
            onDismissRequest = { showVarianteSelector = false },
            title = { Text("Seleccionar Variante", fontWeight = FontWeight.Bold) },
            text = {
                when (variantesStates) {
                    is VariantesStates.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is VariantesStates.Success -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            val variantes = (variantesStates as VariantesStates.Success).variantes
                            variantes.forEach { variante ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            cambiarVariante(variante.idVarianteVisual)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (variante.idVarianteVisual == producto.varianteVisualId)
                                            Color(0xFFE3F2FD) else Color.White
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                            color = Color(0xFFF0F0F0)
                                        ) {
                                            if (variante.imagen != null) {
                                                SubcomposeAsyncImage(
                                                    model = variante.imagen,
                                                    contentDescription = variante.modeloNombre,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(variante.modeloNombre, fontWeight = FontWeight.Medium)
                                            Text(variante.colorPrimarioNombre, fontSize = 12.sp, color = Color.Gray)
                                        }
                                        if (variante.idVarianteVisual == producto.varianteVisualId) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is VariantesStates.Error -> {
                        Text("Error al cargar variantes", color = Color.Red)
                    }
                    else -> {}
                }
            },
            confirmButton = {
                TextButton(onClick = { showVarianteSelector = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun InfoCardEditable(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    isNumeric: Boolean = false,
    isPrecio: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = AquamarinePrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = AquamarinePrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                if (isEditing) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = if (isNumeric) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                    )
                } else {
                    Text(
                        text = if (isPrecio && value.isNotEmpty()) "$${String.format("%,d", value.toIntOrNull() ?: 0)}" else value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = AquamarinePrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = AquamarinePrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
    }
}