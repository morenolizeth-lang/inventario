package com.example.inventariolt.screen.inventario

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.viewModel.PerfilState
import com.example.inventariolt.viewModel.ProductosState
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.ui.theme.*
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioHomeScreen(
    navController: NavController,
    userId: Long,
    viewModel: UsuarioViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val perfilState by viewModel.perfilState.collectAsState()
    val productosState by viewModel.productosState.collectAsState()

    // Cargar perfil y productos
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)
    }

    // Cargar productos cuando el perfil está listo
    LaunchedEffect(perfilState) {
        if (perfilState is PerfilState.Success) {
            val tiendaId = (perfilState as PerfilState.Success).usuario.tiendaId
            if (tiendaId != null) {
                viewModel.cargarProductos(tiendaId)
            }
        }
    }

    // Recargar productos cuando la pantalla obtiene foco (después de agregar un producto)
    DisposableEffect(Unit) {
        onDispose {
            val state = viewModel.perfilState.value
            if (state is PerfilState.Success) {
                state.usuario.tiendaId?.let { viewModel.cargarProductos(it) }
            }
        }
    }

    var searchText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Filtrar productos por búsqueda
    val filteredProducts = when (productosState) {
        is ProductosState.Success -> {
            val productos = (productosState as ProductosState.Success).productos
            if (searchText.isEmpty()) {
                productos
            } else {
                productos.filter {
                    it.modeloNombre.contains(searchText, ignoreCase = true) ||
                            it.talla.contains(searchText, ignoreCase = true)
                }
            }
        }
        else -> emptyList()
    }

    // Diálogo de cierre de sesión
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

    // Diálogo de configuración de cuenta
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = { Text("Configuración de Cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("• Cambiar contraseña")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Configurar notificaciones")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Privacidad y seguridad")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Idioma")
                }
            },
            confirmButton = {
                TextButton(onClick = { showConfigDialog = false }) { Text("Cerrar") }
            }
        )
    }

    // Diálogo de información
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Acerca de", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inventory,
                        contentDescription = "Logo",
                        modifier = Modifier.size(64.dp),
                        tint = AquamarineDark
                    )
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

    // Modal Drawer (Menú desplegable)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                // Header del menú con datos del usuario
                val usuario = (perfilState as? PerfilState.Success)?.usuario

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AquamarineGradient)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Imagen del usuario
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        if (usuario?.fotoPerfil != null) {
                            SubcomposeAsyncImage(
                                model = usuario.fotoPerfil,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                loading = { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) },
                                error = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray) }
                            )
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Usuario",
                                    modifier = Modifier.size(48.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nombre del usuario
                    Text(
                        text = usuario?.nombre ?: "Cargando...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = usuario?.correo ?: "",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                    label = { Text("Configuración de cuenta", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("actualizar_perfil/$userId")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Style, contentDescription = "Variantes") },
                    label = { Text("Variantes", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("lista_variantes/$userId")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Inventory2, contentDescription = "Modelos") },
                    label = { Text("Modelos", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("lista_modelos/$userId")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Ventas") },
                    label = { Text("Generar Venta", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("generar_venta/$userId")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                    label = { Text("Estadísticas", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            navController.navigate("estadisticas/$userId")
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Info, contentDescription = "Información") },
                    label = { Text("Información de la app", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showInfoDialog = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión") },
                    label = { Text("Cerrar sesión", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            showExitDialog = true
                        }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Versión de la app al final del menú
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Versión 1.0.0",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    HeaderConImagen(
                        titulo = "Inventario",
                        subtitulo = "Control de productos",
                        altura = 180.dp
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .padding(start = 8.dp, top = 8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú",
                            tint = Color.White
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                        label = { Text("Inicio") },
                        selected = true,
                        onClick = { }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = "Agregar") },
                        label = { Text("Agregar") },
                        selected = false,
                        onClick = {
                            val usuario = (perfilState as? PerfilState.Success)?.usuario
                            val tiendaId = usuario?.tiendaId ?: 0L
                            navController.navigate("agregar_producto/$tiendaId/$userId")
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = {
                            navController.navigate("perfil/$userId")
                        }
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Barra de búsqueda
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Buscar productos...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = { searchText = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquamarinePrimary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }

                // Contador de productos
                when (productosState) {
                    is ProductosState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AquamarinePrimary)
                        }
                    }
                    is ProductosState.Success -> {
                        Text(
                            text = "${filteredProducts.size} productos encontrados",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    is ProductosState.Error -> {
                        Text(
                            text = "Error al cargar productos",
                            fontSize = 14.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    else -> {}
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid de productos (3 columnas)
                when (productosState) {
                    is ProductosState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AquamarinePrimary)
                        }
                    }
                    is ProductosState.Success -> {
                        if (filteredProducts.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay productos",
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "Presiona el botón + para agregar",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp)
                            ) {
                                items(filteredProducts) { product ->
                                    ProductCard(
                                        product = product,
                                        navController = navController,
                                        userId = userId
                                    )
                                }
                            }
                        }
                    }
                    is ProductosState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error al cargar productos",
                                    fontSize = 16.sp,
                                    color = Color.Red
                                )
                                    Button(
                                        onClick = {
                                            val state = viewModel.perfilState.value
                                            if (state is PerfilState.Success) {
                                                state.usuario.tiendaId?.let { viewModel.cargarProductos(it) }
                                            } else {
                                                viewModel.cargarProductos()
                                            }
                                        },
                                        modifier = Modifier.padding(top = 16.dp)
                                    ) {
                                    Text("Reintentar")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: com.example.inventariolt.model.inventario.ProductoResponseDTO,
    navController: NavController,
    userId: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable {
                val productoJson = Uri.encode(Gson().toJson(product))
                navController.navigate("detalle_producto/${userId}/${productoJson}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del producto
            Surface(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                if (product.imagen != null) {
                    SubcomposeAsyncImage(
                        model = product.imagen,
                        contentDescription = product.modeloNombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        },
                        error = {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Gray
                                )
                            }
                        }
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = product.modeloNombre,
                            modifier = Modifier.size(48.dp),
                            tint = AquamarineDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del producto
            Text(
                text = product.modeloNombre,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // Talla
            Text(
                text = "Talla: ${product.talla}",
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Precio del producto
            Text(
                text = "$${String.format("%,d", product.precioVenta)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AquamarineDark
            )

            // Stock
            if (product.stock <= 5) {
                Text(
                    text = "Stock: ${product.stock}",
                    fontSize = 10.sp,
                    color = if (product.stock == 0) Color.Red else Color(0xFFFF9800)
                )
            }
        }
    }
}