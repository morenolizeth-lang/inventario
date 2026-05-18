package com.example.inventariolt.screen.inventario

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.CategoriasState
import com.example.inventariolt.viewModel.CrearModeloState
import com.example.inventariolt.viewModel.GenerosState
import com.example.inventariolt.viewModel.MarcasState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearModeloScreen(
    navController: NavController,
    viewModel: AgregarProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var nombreModelo by remember { mutableStateOf("") }
    var marcaId by remember { mutableStateOf<Long?>(null) }
    var categoriaId by remember { mutableStateOf<Long?>(null) }
    var generoId by remember { mutableStateOf<Long?>(null) }

    // Estados de expansión
    var expandedMarca by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedGenero by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val marcasState by viewModel.marcasState.collectAsState()
    val categoriasState by viewModel.categoriasState.collectAsState()
    val generosState by viewModel.generosState.collectAsState()
    val crearModeloState by viewModel.crearModeloState.collectAsState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
    }

    // Manejar creación de modelo
    LaunchedEffect(crearModeloState) {
        when (crearModeloState) {
            is CrearModeloState.Success -> {
                Toast.makeText(context, "Modelo creado exitosamente", Toast.LENGTH_SHORT).show()
                viewModel.cargarModelos()
                navController.popBackStack()
            }
            is CrearModeloState.Error -> {
                Toast.makeText(context, (crearModeloState as CrearModeloState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Crear Modelo",
                    subtitulo = "Registra un nuevo modelo",
                    altura = 180.dp
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(start = 8.dp, top = 8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del Modelo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Información del Modelo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del modelo
                    OutlinedTextField(
                        value = nombreModelo,
                        onValueChange = { nombreModelo = it },
                        label = { Text("Nombre del Modelo *") },
                        placeholder = { Text("Ej: Air Max, Classic, Running Pro") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Style, contentDescription = null) },
                        singleLine = true
                    )
                }
            }

            // Selector de Marca
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Marca",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón desplegable
                    OutlinedButton(
                        onClick = { expandedMarca = !expandedMarca },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AquamarinePrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Business,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = getSelectedMarcaName(marcasState, marcaId),
                                    fontSize = 14.sp,
                                    fontWeight = if (marcaId != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (marcaId != null) AquamarinePrimary else Color.Gray
                                )
                            }
                            Icon(
                                if (expandedMarca) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandedMarca) "Contraer" else "Expandir",
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    // Lista desplegable de marcas
                    if (expandedMarca) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val marcasStateValue = marcasState
                        when (marcasStateValue) {
                            is MarcasState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is MarcasState.Success -> {
                                val marcas = marcasStateValue.marcas
                                if (marcas.isNotEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Selecciona una marca:",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            marcas.forEach { marca ->
                                                MarcaItemCard(
                                                    marca = marca,
                                                    isSelected = marcaId == marca.idMarca,
                                                    onClick = {
                                                        marcaId = marca.idMarca
                                                        expandedMarca = false
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
                                        Text(
                                            text = "No hay marcas disponibles.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is MarcasState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar marcas",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Selector de Categoría
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Categoría",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón desplegable
                    OutlinedButton(
                        onClick = { expandedCategoria = !expandedCategoria },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AquamarinePrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = getSelectedCategoriaName(categoriasState, categoriaId),
                                    fontSize = 14.sp,
                                    fontWeight = if (categoriaId != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (categoriaId != null) AquamarinePrimary else Color.Gray
                                )
                            }
                            Icon(
                                if (expandedCategoria) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandedCategoria) "Contraer" else "Expandir",
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    // Lista desplegable de categorías
                    if (expandedCategoria) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val categoriasStateValue = categoriasState
                        when (categoriasStateValue) {
                            is CategoriasState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is CategoriasState.Success -> {
                                val categorias = categoriasStateValue.categorias
                                if (categorias.isNotEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Selecciona una categoría:",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            categorias.forEach { categoria ->
                                                CategoriaItemCard(
                                                    categoria = categoria,
                                                    isSelected = categoriaId == categoria.idCategoria,
                                                    onClick = {
                                                        categoriaId = categoria.idCategoria
                                                        expandedCategoria = false
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
                                        Text(
                                            text = "No hay categorías disponibles.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is CategoriasState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar categorías",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // Selector de Género
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Género",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón desplegable
                    OutlinedButton(
                        onClick = { expandedGenero = !expandedGenero },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AquamarinePrimary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = getSelectedGeneroName(generosState, generoId),
                                    fontSize = 14.sp,
                                    fontWeight = if (generoId != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (generoId != null) AquamarinePrimary else Color.Gray
                                )
                            }
                            Icon(
                                if (expandedGenero) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandedGenero) "Contraer" else "Expandir",
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    // Lista desplegable de géneros
                    if (expandedGenero) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val generosStateValue = generosState
                        when (generosStateValue) {
                            is GenerosState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is GenerosState.Success -> {
                                val generos = generosStateValue.generos
                                if (generos.isNotEmpty()) {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Text(
                                                text = "Selecciona un género:",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            generos.forEach { genero ->
                                                GeneroItemCard(
                                                    genero = genero,
                                                    isSelected = generoId == genero.idGenero,
                                                    onClick = {
                                                        generoId = genero.idGenero
                                                        expandedGenero = false
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
                                        Text(
                                            text = "No hay géneros disponibles.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is GenerosState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar géneros",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    when {
                        nombreModelo.isBlank() -> Toast.makeText(context, "Ingresa el nombre del modelo", Toast.LENGTH_SHORT).show()
                        marcaId == null -> Toast.makeText(context, "Selecciona una marca", Toast.LENGTH_SHORT).show()
                        categoriaId == null -> Toast.makeText(context, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                        generoId == null -> Toast.makeText(context, "Selecciona un género", Toast.LENGTH_SHORT).show()
                        else -> {
                            viewModel.crearModelo(
                                nombre = nombreModelo,
                                marcaId = marcaId!!,
                                categoriaId = categoriaId!!,
                                generoId = generoId!!
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                enabled = crearModeloState !is CrearModeloState.Loading
            ) {
                if (crearModeloState is CrearModeloState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("CREAR MODELO", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MarcaItemCard(
    marca: com.example.inventariolt.model.inventario.MarcaResponseDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AquamarinePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = marca.nombre,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = AquamarineDark,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CategoriaItemCard(
    categoria: com.example.inventariolt.model.inventario.CategoriaResponseDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AquamarinePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = categoria.nombre,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = AquamarineDark,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun GeneroItemCard(
    genero: com.example.inventariolt.model.inventario.GeneroResponseDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AquamarinePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = genero.nombre,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = AquamarineDark,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Funciones auxiliares para obtener nombres seleccionados
private fun getSelectedMarcaName(state: MarcasState, selectedId: Long?): String {
    if (selectedId == null) return "Seleccionar"
    return when (state) {
        is MarcasState.Success -> state.marcas.find { it.idMarca == selectedId }?.nombre ?: "Seleccionar"
        else -> "Seleccionar"
    }
}

private fun getSelectedCategoriaName(state: CategoriasState, selectedId: Long?): String {
    if (selectedId == null) return "Seleccionar"
    return when (state) {
        is CategoriasState.Success -> state.categorias.find { it.idCategoria == selectedId }?.nombre ?: "Seleccionar"
        else -> "Seleccionar"
    }
}

private fun getSelectedGeneroName(state: GenerosState, selectedId: Long?): String {
    if (selectedId == null) return "Seleccionar"
    return when (state) {
        is GenerosState.Success -> state.generos.find { it.idGenero == selectedId }?.nombre ?: "Seleccionar"
        else -> "Seleccionar"
    }
}