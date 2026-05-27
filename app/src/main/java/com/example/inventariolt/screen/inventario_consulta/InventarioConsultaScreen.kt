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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.text.style.TextOverflow
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

    val categorias by viewModel.categoriasState.collectAsState()
    val marcas by viewModel.marcasState.collectAsState()
    val generos by viewModel.generosState.collectAsState()
    val modelos by viewModel.modelosState.collectAsState()

    var selectedTiendaId by remember { mutableStateOf<Long?>(null) }
    var selectedTiendaNombre by remember { mutableStateOf("Todas las tiendas") }
    var showTiendaDialog by remember { mutableStateOf(false) }

    var selectedCategoria by remember { mutableStateOf<String?>(null) }
    var selectedMarca by remember { mutableStateOf<String?>(null) }
    var selectedGenero by remember { mutableStateOf<String?>(null) }
    var selectedTalla by remember { mutableStateOf<String?>(null) }
    var expandedFilter by remember { mutableStateOf<String?>(null) }
    var sortByLowStock by remember { mutableStateOf(false) }
    var sortByCheapest by remember { mutableStateOf(false) }

    val tallasColombia = listOf("34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "6", "8", "10", "12", "14", "16")

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

    val filteredProducts = remember(productosState, searchText, selectedCategoria, selectedMarca, selectedGenero, selectedTalla, sortByLowStock, sortByCheapest, modelos) {
        when (productosState) {
            is ProductosState.Success -> {
                var productos = (productosState as ProductosState.Success).productos
                val query = searchText.trim()

                if (query.isNotEmpty()) {
                    val modelsMatchingMetadata = modelos.filter {
                        it.generoNombre.contains(query, ignoreCase = true) ||
                        it.categoriaNombre.contains(query, ignoreCase = true) ||
                        it.marcaNombre.contains(query, ignoreCase = true)
                    }.map { it.idModelo }.toSet()

                    productos = productos.filter {
                        (it.modeloNombre?.contains(query, ignoreCase = true) == true) ||
                        (it.talla?.contains(query, ignoreCase = true) == true) ||
                        (it.marcaNombre?.contains(query, ignoreCase = true) == true) ||
                        (it.colorPrimarioNombre?.contains(query, ignoreCase = true) == true) ||
                        (it.colorSecundarioNombre?.contains(query, ignoreCase = true) == true) ||
                        (it.modeloId in modelsMatchingMetadata)
                    }
                }

                selectedCategoria?.let { cat ->
                    val catModels = modelos.filter { it.categoriaNombre.trim().contains(cat.trim(), ignoreCase = true) }.map { it.idModelo }.toSet()
                    productos = productos.filter { it.modeloId in catModels }
                }

                selectedMarca?.let { marca ->
                    val brandModels = modelos.filter { it.marcaNombre.trim().contains(marca.trim(), ignoreCase = true) }.map { it.idModelo }.toSet()
                    productos = productos.filter { 
                        (it.marcaNombre?.trim()?.contains(marca.trim(), ignoreCase = true) == true) || 
                        (it.modeloId in brandModels)
                    }
                }

                selectedGenero?.let { gen ->
                    val genModels = modelos.filter { it.generoNombre.trim().contains(gen.trim(), ignoreCase = true) }.map { it.idModelo }.toSet()
                    productos = productos.filter { it.modeloId in genModels }
                }

                selectedTalla?.let { talla ->
                    productos = productos.filter { it.talla?.trim()?.contains(talla.trim(), ignoreCase = true) == true }
                }

                if (sortByLowStock) productos = productos.filter { it.stock <= 5 }.sortedBy { it.stock }
                if (sortByCheapest) productos = productos.sortedBy { it.precioVenta }

                productos
            }
            else -> emptyList()
        }
    }

    if (showTiendaDialog) {
        Dialog(onDismissRequest = { showTiendaDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seleccionar Tienda", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AquamarinePrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).weight(1f, fill = false)) {
                        TiendaItem("Todas las tiendas", selectedTiendaId == null) {
                            selectedTiendaId = null
                            selectedTiendaNombre = "Todas las tiendas"
                            viewModel.cargarProductos(null)
                            showTiendaDialog = false
                        }
                        if (tiendasState is TiendasState.Success) {
                            (tiendasState as TiendasState.Success).tiendas.forEach { tienda ->
                                TiendaItem(tienda.nombre, selectedTiendaId == tienda.idTienda) {
                                    selectedTiendaId = tienda.idTienda
                                    selectedTiendaNombre = tienda.nombre
                                    viewModel.cargarProductos(tienda.idTienda)
                                    showTiendaDialog = false
                                }
                            }
                        }
                    }
                    TextButton(onClick = { showTiendaDialog = false }, modifier = Modifier.align(Alignment.End)) { Text("Cancelar", color = Color.Gray) }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                Button(onClick = { showExitDialog = false; navController.navigate("login") { popUpTo("login") { inclusive = true } } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Sí, cerrar sesión") }
            },
            dismissButton = { TextButton(onClick = { showExitDialog = false }) { Text("Cancelar") } }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Acerca de", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = AquamarineDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sistema de Inventario", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Versión 1.0.0", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Desarrollado por: Lizeth M. & Tomas P.", fontSize = 14.sp)
                }
            },
            confirmButton = { TextButton(onClick = { showInfoDialog = false }) { Text("Cerrar") } }
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp), drawerContainerColor = Color.White) {
                val usuario = (perfilState as? PerfilState.Success)?.usuario
                Column(modifier = Modifier.fillMaxWidth().background(AquamarineGradient).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.size(80.dp), shape = CircleShape, color = Color.White, shadowElevation = 4.dp) {
                        if (usuario?.fotoPerfil != null) {
                            SubcomposeAsyncImage(model = usuario.fotoPerfil, contentDescription = null, modifier = Modifier.clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(usuario?.nombre ?: "Cargando...", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(usuario?.correo ?: "", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Configuración") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); navController.navigate("actualizar_perfil/$userId") } },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Logout, null) },
                    label = { Text("Cerrar sesión") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close(); showExitDialog = true } },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box {
                    HeaderConImagen(titulo = "Consulta", subtitulo = selectedTiendaNombre, altura = 180.dp) {
                        Icon(Icons.Default.Inventory, null, modifier = Modifier.size(48.dp), tint = Color.White)
                    }
                    IconButton(onClick = { scope.launch { drawerState.open() } }, modifier = Modifier.padding(8.dp).align(Alignment.TopStart)) {
                        Icon(Icons.Default.Menu, null, tint = Color.White)
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(icon = { Icon(Icons.Default.Home, null) }, label = { Text("Inicio") }, selected = true, onClick = {})
                    NavigationBarItem(icon = { Icon(Icons.Default.Store, null) }, label = { Text("Tienda") }, selected = false, onClick = { showTiendaDialog = true })
                    NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") }, selected = false, onClick = { navController.navigate("perfil/$userId") })
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF5F5F5))) {
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(12.dp)) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("¿Qué estás buscando?") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { if (searchText.isNotEmpty()) IconButton(onClick = { searchText = "" }) { Icon(Icons.Default.Close, null) } },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color.Transparent)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MainFilterChip("Categoría", expandedFilter == "Categoría", selectedCategoria != null) { expandedFilter = if (expandedFilter == "Categoría") null else "Categoría" }
                    MainFilterChip("Marca", expandedFilter == "Marca", selectedMarca != null) { expandedFilter = if (expandedFilter == "Marca") null else "Marca" }
                    MainFilterChip("Género", expandedFilter == "Género", selectedGenero != null) { expandedFilter = if (expandedFilter == "Género") null else "Género" }
                    MainFilterChip("Talla", expandedFilter == "Talla", selectedTalla != null) { expandedFilter = if (expandedFilter == "Talla") null else "Talla" }
                    MainFilterChip("Ordenar", expandedFilter == "Ordenar", sortByLowStock || sortByCheapest) { expandedFilter = if (expandedFilter == "Ordenar") null else "Ordenar" }
                }

                if (expandedFilter != null) {
                    Surface(modifier = Modifier.fillMaxWidth().padding(8.dp), color = Color.White.copy(alpha = 0.8f)) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            when (expandedFilter) {
                                "Categoría" -> SubFilterRow(categorias.map { it.nombre }, selectedCategoria) { selectedCategoria = it; expandedFilter = null }
                                "Marca" -> SubFilterRow(marcas.map { it.nombre }, selectedMarca) { selectedMarca = it; expandedFilter = null }
                                "Género" -> SubFilterRow(generos.map { it.nombre }, selectedGenero) { selectedGenero = it; expandedFilter = null }
                                "Talla" -> SubFilterRow(tallasColombia, selectedTalla) { selectedTalla = it; expandedFilter = null }
                                "Ordenar" -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(sortByLowStock, { sortByLowStock = !sortByLowStock; expandedFilter = null }, { Text("Poco Stock") })
                                    FilterChip(sortByCheapest, { sortByCheapest = !sortByCheapest; expandedFilter = null }, { Text("Más Económicos") })
                                }
                            }
                        }
                    }
                }

                if (selectedCategoria != null || selectedMarca != null || selectedGenero != null || selectedTalla != null) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        selectedCategoria?.let { ActiveFilterChip("Cat: $it") { selectedCategoria = null } }
                        selectedMarca?.let { ActiveFilterChip("Marca: $it") { selectedMarca = null } }
                        selectedGenero?.let { ActiveFilterChip("Gen: $it") { selectedGenero = null } }
                        selectedTalla?.let { ActiveFilterChip("Talla: $it") { selectedTalla = null } }
                    }
                }

                if (productosState is ProductosState.Success) {
                    Text("${filteredProducts.size} productos encontrados", modifier = Modifier.padding(horizontal = 16.dp), fontSize = 12.sp, color = Color.Gray)
                    LazyVerticalGrid(columns = GridCells.Fixed(3), contentPadding = PaddingValues(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredProducts) { product -> ProductCard(product, navController, userId) }
                    }
                } else if (productosState is ProductosState.Loading) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = AquamarinePrimary) }
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
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AquamarinePrimary,
            selectedLabelColor = Color.White,
            containerColor = if (hasValue) AquamarinePrimary.copy(alpha = 0.1f) else Color.White
        )
    )
}

@Composable
fun SubFilterRow(items: List<String>, selectedItem: String?, onItemSelected: (String?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            FilterChip(selected = selectedItem == item, onClick = { onItemSelected(if (selectedItem == item) null else item) }, label = { Text(item, fontSize = 12.sp) })
        }
    }
}

@Composable
fun ActiveFilterChip(text: String, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = AquamarinePrimary.copy(alpha = 0.1f), border = androidx.compose.foundation.BorderStroke(1.dp, AquamarinePrimary)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, fontSize = 11.sp, color = AquamarineDark)
            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp).clickable { onRemove() }, tint = AquamarineDark)
        }
    }
}

@Composable
fun TiendaItem(nombre: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Store, null, tint = if (isSelected) AquamarinePrimary else Color.Gray)
        Spacer(Modifier.width(12.dp))
        Text(nombre, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) AquamarinePrimary else Color.Black)
    }
}
