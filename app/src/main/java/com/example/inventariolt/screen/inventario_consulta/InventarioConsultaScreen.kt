package com.example.inventariolt.screen.inventario_consulta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.viewModel.PerfilState
import com.example.inventariolt.viewModel.ProductosState
import com.example.inventariolt.viewModel.TiendasState
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.screen.inventario.ProductCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioConsultaScreen(
    navController: NavController,
    userId: Long,
    viewModel: UsuarioViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val perfilState by viewModel.perfilState.collectAsState()
    val productosState by viewModel.productosState.collectAsState()
    val tiendasState by viewModel.tiendasState.collectAsState()

    // Estados para catálogos de filtros
    val categorias by viewModel.categoriasState.collectAsState()
    val marcas by viewModel.marcasState.collectAsState()
    val generos by viewModel.generosState.collectAsState()
    val modelos by viewModel.modelosState.collectAsState()

    var selectedTiendaId by remember { mutableStateOf<Long?>(null) }
    var selectedTiendaNombre by remember { mutableStateOf("Todas las tiendas") }
    var showTiendaDialog by remember { mutableStateOf(false) }

    // Estados de filtros seleccionados
    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var selectedMarca by remember { mutableStateOf<String?>(null) }
    var selectedGenero by remember { mutableStateOf<String?>(null) }
    var selectedTalla by remember { mutableStateOf<String?>(null) }
    var expandedFilter by remember { mutableStateOf<String?>(null) }
    var sortByLowStock by remember { mutableStateOf(false) }
    var sortByCheapest by remember { mutableStateOf(false) }

    val tallasColombia = listOf("34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "6", "8", "10", "12", "14", "16")

    // Cargar perfil y productos iniciales
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)
        viewModel.cargarProductos(null)
        viewModel.cargarTiendas()
        viewModel.cargarFiltros()
    }

    var searchText by remember { mutableStateOf("") }
    var showExitDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Filtrar productos por búsqueda y filtros
    val filteredProducts = remember(productosState, searchText, selectedCategoria, selectedMarca, selectedGenero, selectedTalla, sortByLowStock, sortByCheapest, modelos) {
        when (productosState) {
            is ProductosState.Success -> {
                var productos = (productosState as ProductosState.Success).productos
                
                // Filtro por búsqueda (Modelo, Talla, Marca) con protección extrema contra Nulos
                val query = searchText.trim()
                if (query.isNotEmpty()) {
                    productos = productos.filter {
                        (it.modeloNombre?.contains(query, ignoreCase = true) == true) ||
                        (it.talla?.contains(query, ignoreCase = true) == true) ||
                        (it.marcaNombre?.contains(query, ignoreCase = true) == true)
                    }
                }

                // Filtro por Categoría (requiere cruce con modelos)
                if (selectedCategoria != null) {
                    val modelosEnCategoria = modelos.filter { it.categoriaNombre == selectedCategoria }.map { it.idModelo }
                    productos = productos.filter { it.modeloId in modelosEnCategoria }
                }

                // Filtro por Marca (Comparación tolerante y segura contra nulos)
                if (selectedMarca != null) {
                    val m = selectedMarca // Local copy for smart cast
                    productos = productos.filter { it.marcaNombre?.trim()?.equals(m?.trim(), ignoreCase = true) == true }
                }

                // Filtro por Género (requiere cruce con modelos)
                if (selectedGenero != null) {
                    val modelosGenero = modelos.filter { it.generoNombre == selectedGenero }.map { it.idModelo }
                    productos = productos.filter { it.modeloId in modelosGenero }
                }

                // Filtro por Talla
                if (selectedTalla != null) {
                    productos = productos.filter { it.talla == selectedTalla }
                }

                // Ordenamiento / Filtros especiales
                if (sortByLowStock) {
                    productos = productos.filter { it.stock <= 5 }.sortedBy { it.stock }
                }
                
                if (sortByCheapest) {
                    // Ordenar por precio y tomar el top 10 o similar si se desea limitar, 
                    // o simplemente ordenar ascendentemente.
                    productos = productos.sortedBy { it.precioVenta }
                }

                productos
            }
            else -> emptyList()
        }
    }


    // Diálogo de selección de tienda
    if (showTiendaDialog) {
        Dialog(onDismissRequest = { showTiendaDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Seleccionar Tienda",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AquamarinePrimary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .weight(1f, fill = false)
                    ) {
                        TiendaItem(
                            nombre = "Todas las tiendas",
                            isSelected = selectedTiendaId == null,
                            onClick = {
                                selectedTiendaId = null
                                selectedTiendaNombre = "Todas las tiendas"
                                viewModel.cargarProductos(null)
                                showTiendaDialog = false
                            }
                        )
                        
                        when (tiendasState) {
                            is TiendasState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is TiendasState.Success -> {
                                (tiendasState as TiendasState.Success).tiendas.forEach { tienda ->
                                    TiendaItem(
                                        nombre = tienda.nombre,
                                        isSelected = selectedTiendaId == tienda.idTienda,
                                        onClick = {
                                            selectedTiendaId = tienda.idTienda
                                            selectedTiendaNombre = tienda.nombre
                                            viewModel.cargarProductos(tienda.idTienda)
                                            showTiendaDialog = false
                                        }
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showTiendaDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancelar", color = Color.Gray)
                    }
                }
            }
        }
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AquamarineGradient)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    Text(
                        text = usuario?.nombre ?: "Cargando...",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    if (usuario != null) {
                        Text(
                            text = when (usuario.rol) {
                                "CONSULTA" -> "EMPLEADO"
                                "EMPLEADO" -> "ADMIN TIENDA"
                                else -> usuario.rol
                            },
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

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

                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        titulo = "Consulta",
                        subtitulo = selectedTiendaNombre,
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
                        icon = { Icon(Icons.Default.Store, contentDescription = "Tienda") },
                        label = { Text("Tienda") },
                        selected = false,
                        onClick = { showTiendaDialog = true }
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("¿Qué estás buscando?", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquamarinePrimary) },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = { searchText = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AquamarinePrimary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }

                // Categorías de Filtros (Chips principales)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainFilterChip(
                        label = "Categoría",
                        isSelected = expandedFilter == "Categoría",
                        hasValue = selectedCategoria != null,
                        onClick = { expandedFilter = if (expandedFilter == "Categoría") null else "Categoría" }
                    )
                    MainFilterChip(
                        label = "Marca",
                        isSelected = expandedFilter == "Marca",
                        hasValue = selectedMarca != null,
                        onClick = { expandedFilter = if (expandedFilter == "Marca") null else "Marca" }
                    )
                    MainFilterChip(
                        label = "Género",
                        isSelected = expandedFilter == "Género",
                        hasValue = selectedGenero != null,
                        onClick = { expandedFilter = if (expandedFilter == "Género") null else "Género" }
                    )
                    MainFilterChip(
                        label = "Talla",
                        isSelected = expandedFilter == "Talla",
                        hasValue = selectedTalla != null,
                        onClick = { expandedFilter = if (expandedFilter == "Talla") null else "Talla" }
                    )
                    MainFilterChip(
                        label = "Ordenar",
                        isSelected = expandedFilter == "Ordenar",
                        hasValue = sortByLowStock || sortByCheapest,
                        onClick = { expandedFilter = if (expandedFilter == "Ordenar") null else "Ordenar" }
                    )

                    if (selectedCategoria != null || selectedMarca != null || selectedGenero != null || selectedTalla != null || sortByLowStock || sortByCheapest) {
                        TextButton(
                            onClick = {
                                selectedCategoria = null
                                selectedMarca = null
                                selectedGenero = null
                                selectedTalla = null
                                sortByLowStock = false
                                sortByCheapest = false
                                expandedFilter = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Limpiar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }

                // Opciones expandidas
                if (expandedFilter != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            when (expandedFilter) {
                                "Categoría" -> {
                                    SubFilterRow(categorias.map { it.nombre }, selectedCategoria) {
                                        selectedCategoria = it
                                        expandedFilter = null
                                    }
                                }
                                "Marca" -> {
                                    SubFilterRow(marcas.map { it.nombre }, selectedMarca) {
                                        selectedMarca = it
                                        expandedFilter = null
                                    }
                                }
                                "Género" -> {
                                    SubFilterRow(generos.map { it.nombre }, selectedGenero) {
                                        selectedGenero = it
                                        expandedFilter = null
                                    }
                                }
                                "Talla" -> {
                                    SubFilterRow(tallasColombia, selectedTalla) {
                                        selectedTalla = it
                                        expandedFilter = null
                                    }
                                }
                                "Ordenar" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilterChip(
                                            selected = sortByLowStock,
                                            onClick = { sortByLowStock = !sortByLowStock; expandedFilter = null },
                                            label = { Text("Poco Stock") },
                                            leadingIcon = { if (sortByLowStock) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                        )
                                        FilterChip(
                                            selected = sortByCheapest,
                                            onClick = { sortByCheapest = !sortByCheapest; expandedFilter = null },
                                            label = { Text("Más Económicos") },
                                            leadingIcon = { if (sortByCheapest) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chips de filtros activos (compactos)
                if (selectedCategoria != null || selectedMarca != null || selectedGenero != null || selectedTalla != null || sortByLowStock || sortByCheapest) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (selectedCategoria != null) ActiveFilterChip("Cat: $selectedCategoria") { selectedCategoria = null }
                        if (selectedMarca != null) ActiveFilterChip("Marca: $selectedMarca") { selectedMarca = null }
                        if (selectedGenero != null) ActiveFilterChip("Gen: $selectedGenero") { selectedGenero = null }
                        if (selectedTalla != null) ActiveFilterChip("Talla: $selectedTalla") { selectedTalla = null }
                        if (sortByLowStock) ActiveFilterChip("Poco Stock") { sortByLowStock = false }
                        if (sortByCheapest) ActiveFilterChip("Económicos") { sortByCheapest = false }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Contador de productos
                when (productosState) {
                    is ProductosState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
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
                    else -> {}
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grid de productos
                when (productosState) {
                    is ProductosState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AquamarinePrimary)
                        }
                    }
                    is ProductosState.Success -> {
                        if (filteredProducts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("No hay productos con estos filtros", color = Color.Gray)
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = Color.Red)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Error al cargar productos", color = Color.Red)
                                Button(onClick = { viewModel.cargarProductos(selectedTiendaId) }, modifier = Modifier.padding(top = 16.dp)) {
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
fun MainFilterChip(label: String, isSelected: Boolean, hasValue: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        trailingIcon = {
            Icon(
                imageVector = if (isSelected) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AquamarinePrimary,
            selectedLabelColor = Color.White,
            containerColor = if (hasValue) AquamarinePrimary.copy(alpha = 0.1f) else Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (hasValue) AquamarinePrimary else Color.LightGray,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}

@Composable
fun SubFilterRow(items: List<String>, selectedItem: String?, onItemSelected: (String?) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            FilterChip(
                selected = selectedItem == item,
                onClick = { onItemSelected(if (selectedItem == item) null else item) },
                label = { Text(item, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AquamarinePrimary.copy(alpha = 0.2f),
                    selectedLabelColor = AquamarinePrimary
                )
            )
        }
    }
}

@Composable
fun ActiveFilterChip(text: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = AquamarinePrimary.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, AquamarinePrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 12.sp, color = AquamarineDark)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(14.dp).clickable { onRemove() },
                tint = AquamarineDark
            )
        }
    }
}

@Composable
fun TiendaItem(nombre: String, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AquamarinePrimary.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AquamarinePrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Store,
                contentDescription = null,
                tint = if (isSelected) AquamarinePrimary else Color.Gray
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = nombre,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) AquamarinePrimary else Color.Black
            )
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Check, null, tint = AquamarinePrimary)
            }
        }
    }
}
