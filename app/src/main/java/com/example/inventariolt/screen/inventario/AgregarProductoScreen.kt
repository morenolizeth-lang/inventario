package com.example.inventariolt.screen.inventario

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.inventariolt.model.inventario.ModeloResponseDTO
import com.example.inventariolt.model.inventario.VarianteVisualResponseDTO
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.CrearProductoState
import com.example.inventariolt.viewModel.PerfilState
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.viewModel.VariantesConModeloState
import com.example.inventariolt.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarProductoScreen(
    navController: NavController,
    tiendaId: Long,
    userId: Long,
    viewModel: AgregarProductoViewModel = viewModel(),
    usuarioViewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var varianteSeleccionadaId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var talla by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var precioCompra by remember { mutableStateOf("") }
    var precioVenta by remember { mutableStateOf("") }
    var varianteSeleccionada by remember { mutableStateOf<VarianteVisualResponseDTO?>(null) }

    val variantesConModeloState by viewModel.variantesConModeloState.collectAsState()
    val crearProductoState by viewModel.crearProductoState.collectAsState()
    val perfilState by usuarioViewModel.perfilState.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        usuarioViewModel.cargarPerfil(userId)
        viewModel.cargarVariantesConModelo()
    }

    LaunchedEffect(crearProductoState) {
        when (crearProductoState) {
            is CrearProductoState.Success -> {
                Toast.makeText(context, "Producto agregado exitosamente", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is CrearProductoState.Error -> {
                Toast.makeText(context, (crearProductoState as CrearProductoState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Diálogos (omitidos por brevedad)
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar Sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, cerrar sesión") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Acerca de", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, contentDescription = "Logo", modifier = Modifier.size(64.dp), tint = AquamarinePrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sistema de Inventario", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Versión 1.0.0", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Desarrollado por:", fontWeight = FontWeight.Bold)
                    Text("Lizeth M. & Tomas P.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("© 2026 - Todos los derechos reservados", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") }
            }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                val usuario = (perfilState as? PerfilState.Success)?.usuario
                Column(
                    modifier = Modifier.fillMaxWidth().background(AquamarineGradient).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(50.dp), color = Color.White, shadowElevation = 4.dp) {
                        if (usuario?.fotoPerfil != null) {
                            SubcomposeAsyncImage(
                                model = usuario.fotoPerfil,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                loading = { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) },
                                error = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray) }
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = "Usuario", modifier = Modifier.size(48.dp), tint = Color.Gray)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = usuario?.nombre ?: "Cargando...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Text(text = usuario?.correo ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                    label = { Text("Configuración de cuenta", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); navController.navigate("actualizar_perfil/$userId") } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Variantes") },
                    label = { Text("Variantes", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); navController.navigate("lista_variantes") } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                // En InventarioHomeScreen, dentro del ModalDrawerSheet, después de NavigationDrawerItem de Variantes
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Modelos") },
                    label = { Text("Modelos", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("lista_modelos")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ventas") },
                    label = { Text("Generar Venta", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); navController.navigate("generar_venta/$userId") } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                    label = { Text("Estadísticas", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); navController.navigate("estadisticas/$userId") } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Info, contentDescription = "Información") },
                    label = { Text("Información de la app", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); showInfoDialog = true } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión") },
                    label = { Text("Cerrar sesión", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); showExitDialog = true } },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Versión 1.0.0", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    HeaderConImagen(titulo = "Agregar Producto", subtitulo = "Completa los datos", altura = 180.dp)
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.padding(start = 8.dp, top = 8.dp).align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = false,
                        onClick = { navController.navigate("inventario_home/$userId") { popUpTo("agregar_producto/$tiendaId/$userId") { inclusive = true } } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                        label = { Text("Agregar") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = { navController.navigate("perfil/$userId") }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta de selección de variante
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Variante Visual", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarinePrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarinePrimary)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (varianteSeleccionada != null)
                                            "${varianteSeleccionada!!.modeloNombre} - ${varianteSeleccionada!!.colorPrimarioNombre}"
                                        else "Seleccione una variante",
                                        fontSize = 14.sp,
                                        fontWeight = if (varianteSeleccionada != null) FontWeight.Medium else FontWeight.Normal,
                                        color = if (varianteSeleccionada != null) AquamarinePrimary else Color.Gray
                                    )
                                }
                                Icon(
                                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Contraer" else "Expandir",
                                    tint = AquamarinePrimary
                                )
                            }
                        }

                        if (expanded) {
                            Spacer(modifier = Modifier.height(12.dp))
                            // Usar variable local para smart cast
                            val currentState = variantesConModeloState
                            when (currentState) {
                                is VariantesConModeloState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = AquamarinePrimary)
                                    }
                                }
                                is VariantesConModeloState.Success -> {
                                    val items = currentState.variantes
                                    if (items.isNotEmpty()) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.padding(8.dp)
                                            ) {
                                                Text("Selecciona una variante:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                                                items.forEach { item ->
                                                    VarianteCardCompleta(
                                                        variante = item.variante,
                                                        modelo = item.modelo,
                                                        isSelected = varianteSeleccionadaId == item.variante.idVarianteVisual,
                                                        onClick = {
                                                            varianteSeleccionadaId = item.variante.idVarianteVisual
                                                            varianteSeleccionada = item.variante
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("No hay variantes disponibles. Crea una nueva.", color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                                is VariantesConModeloState.Error -> {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Error al cargar variantes", color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                    }
                                }
                                else -> {}
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { navController.navigate("crear_variante") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Crear Nueva Variante")
                        }
                    }
                }

                // Campos del producto
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Información del Producto", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarinePrimary)
                        OutlinedTextField(value = talla, onValueChange = { talla = it }, label = { Text("Talla") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null) })
                        OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Stock") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Inventory, contentDescription = null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = precioCompra, onValueChange = { precioCompra = it }, label = { Text("Precio de Compra") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = precioVenta, onValueChange = { precioVenta = it }, label = { Text("Precio de Venta") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (varianteSeleccionada != null && talla.isNotBlank() && stock.isNotBlank() && precioCompra.isNotBlank() && precioVenta.isNotBlank()) {
                            viewModel.crearProducto(
                                varianteVisualId = varianteSeleccionada!!.idVarianteVisual,
                                tiendaId = tiendaId,
                                stock = stock.toIntOrNull() ?: 0,
                                precioCompra = precioCompra.toIntOrNull() ?: 0,
                                precioVenta = precioVenta.toIntOrNull() ?: 0,
                                talla = talla
                            )
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                    enabled = crearProductoState !is CrearProductoState.Loading
                ) {
                    if (crearProductoState is CrearProductoState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("AGREGAR PRODUCTO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VarianteCardCompleta(
    variante: VarianteVisualResponseDTO,
    modelo: ModeloResponseDTO?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                if (variante.imagen != null) {
                    SubcomposeAsyncImage(
                        model = variante.imagen,
                        contentDescription = variante.modeloNombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(20.dp)) } },
                        error = { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray) } }
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color.Gray) }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = variante.modeloNombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AquamarineDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Color: ${variante.colorPrimarioNombre}", fontSize = 11.sp, color = Color.Gray)
                if (variante.colorSecundarioNombre != null) {
                    Text(text = "Secundario: ${variante.colorSecundarioNombre}", fontSize = 11.sp, color = Color.Gray)
                }

                // Mostrar información del modelo si existe
                modelo?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(shape = RoundedCornerShape(4.dp), color = AquamarinePrimary.copy(alpha = 0.1f)) {
                            Text(text = it.marcaNombre, fontSize = 9.sp, color = AquamarinePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = AquamarinePrimary.copy(alpha = 0.1f)) {
                            Text(text = it.categoriaNombre, fontSize = 9.sp, color = AquamarinePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = AquamarinePrimary.copy(alpha = 0.1f)) {
                            Text(text = it.generoNombre, fontSize = 9.sp, color = AquamarinePrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }
    }
}