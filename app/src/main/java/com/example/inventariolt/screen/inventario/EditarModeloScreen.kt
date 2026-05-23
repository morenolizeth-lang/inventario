package com.example.inventariolt.screen.inventario

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import com.example.inventariolt.viewModel.ActualizarModeloState
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.CategoriasState
import com.example.inventariolt.viewModel.EliminarModeloState
import com.example.inventariolt.viewModel.GenerosState
import com.example.inventariolt.viewModel.MarcasState
import com.example.inventariolt.viewModel.ModeloDetalleState
import com.example.inventariolt.viewModel.ModeloViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarModeloScreen(
    navController: NavController,
    modeloId: Long,
    modeloViewModel: ModeloViewModel = viewModel(),
    agregarProductoViewModel: AgregarProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var nombreModelo by remember { mutableStateOf("") }
    var marcaId by remember { mutableStateOf<Long?>(null) }
    var categoriaId by remember { mutableStateOf<Long?>(null) }
    var generoId by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados de error para validación
    var nombreModeloError by remember { mutableStateOf(false) }
    var marcaError by remember { mutableStateOf(false) }
    var categoriaError by remember { mutableStateOf(false) }
    var generoError by remember { mutableStateOf(false) }

    // Estados de expansión
    var expandedMarca by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedGenero by remember { mutableStateOf(false) }

    // Estados para diálogos
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val marcasState by agregarProductoViewModel.marcasState.collectAsState()
    val categoriasState by agregarProductoViewModel.categoriasState.collectAsState()
    val generosState by agregarProductoViewModel.generosState.collectAsState()
    val modeloDetalleState by modeloViewModel.modeloDetalleState.collectAsState()
    val actualizarModeloState by modeloViewModel.actualizarModeloState.collectAsState()
    val eliminarModeloState by modeloViewModel.eliminarModeloState.collectAsState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        agregarProductoViewModel.cargarDatosIniciales()
        modeloViewModel.cargarModeloPorId(modeloId)
    }

    // Observar la carga del modelo
    LaunchedEffect(modeloDetalleState) {
        val currentState = modeloDetalleState
        when (currentState) {
            is ModeloDetalleState.Success -> {
                val modelo = currentState.modelo
                nombreModelo = modelo.nombre
                marcaId = modelo.marcaId
                categoriaId = modelo.categoriaId
                generoId = modelo.generoId
                isLoading = false
            }
            is ModeloDetalleState.Error -> {
                isLoading = false
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
            is ModeloDetalleState.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }

    // Manejar actualización de modelo
    LaunchedEffect(actualizarModeloState) {
        val currentState = actualizarModeloState
        when (currentState) {
            is ActualizarModeloState.Success -> {
                Toast.makeText(context, "Modelo actualizado exitosamente", Toast.LENGTH_SHORT).show()
                modeloViewModel.cargarModelos()
                navController.popBackStack()
            }
            is ActualizarModeloState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                modeloViewModel.resetActualizarModeloState()
            }
            else -> {}
        }
    }

    // Manejar eliminación de modelo
    LaunchedEffect(eliminarModeloState) {
        val currentState = eliminarModeloState
        when (currentState) {
            is EliminarModeloState.Success -> {
                Toast.makeText(context, "Modelo eliminado exitosamente", Toast.LENGTH_SHORT).show()
                modeloViewModel.cargarModelos()
                navController.popBackStack()
            }
            is EliminarModeloState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                modeloViewModel.resetEliminarModeloState()
            }
            else -> {}
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Modelo", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro de que deseas eliminar el modelo \"$nombreModelo\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        modeloViewModel.eliminarModelo(modeloId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Editar Modelo",
                    subtitulo = "Actualiza la información del modelo",
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AquamarinePrimary)
                }
            } else {
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

                        OutlinedTextField(
                            value = nombreModelo,
                            onValueChange = { 
                                nombreModelo = it
                                nombreModeloError = it.isBlank()
                            },
                            label = { Text("Nombre del Modelo *") },
                            placeholder = { Text("Ej: Air Max, Classic, Running Pro") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            isError = nombreModeloError,
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Style, 
                                    contentDescription = null,
                                    tint = if (nombreModeloError) Color.Red else AquamarinePrimary
                                ) 
                            },
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
                        Text("Marca", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarinePrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { expandedMarca = !expandedMarca },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarinePrimary),
                            border = if (marcaError) BorderStroke(2.dp, Color.Red) else ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(20.dp), tint = AquamarinePrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = getSelectedMarcaName(marcasState, marcaId),
                                        fontSize = 14.sp,
                                        fontWeight = if (marcaId != null) FontWeight.Medium else FontWeight.Normal,
                                        color = if (marcaId != null) AquamarinePrimary else Color.Gray
                                    )
                                }
                                Icon(if (expandedMarca) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AquamarinePrimary)
                            }
                        }

                        if (expandedMarca) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val currentMarcasState = marcasState
                            when (currentMarcasState) {
                                is MarcasState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = AquamarinePrimary)
                                    }
                                }
                                is MarcasState.Success -> {
                                    val marcas = currentMarcasState.marcas
                                    if (marcas.isNotEmpty()) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                                                Text("Selecciona una marca:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                                                marcas.forEach { marca ->
                                                    MarcaItemCard(
                                                        marca = marca,
                                                        isSelected = marcaId == marca.idMarca,
                                                        onClick = {
                                                        marcaId = marca.idMarca
                                                        marcaError = false
                                                        expandedMarca = false
                                                    }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                            Text("No hay marcas disponibles.", color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                                is MarcasState.Error -> {
                                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                        Text(currentMarcasState.message, color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
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
                        Text("Categoría", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarinePrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { expandedCategoria = !expandedCategoria },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarinePrimary),
                            border = if (categoriaError) BorderStroke(2.dp, Color.Red) else ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(20.dp), tint = AquamarinePrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = getSelectedCategoriaName(categoriasState, categoriaId),
                                        fontSize = 14.sp,
                                        fontWeight = if (categoriaId != null) FontWeight.Medium else FontWeight.Normal,
                                        color = if (categoriaId != null) AquamarinePrimary else Color.Gray
                                    )
                                }
                                Icon(if (expandedCategoria) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AquamarinePrimary)
                            }
                        }

                        if (expandedCategoria) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val currentCategoriasState = categoriasState
                            when (currentCategoriasState) {
                                is CategoriasState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = AquamarinePrimary)
                                    }
                                }
                                is CategoriasState.Success -> {
                                    val categorias = currentCategoriasState.categorias
                                    if (categorias.isNotEmpty()) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                                                Text("Selecciona una categoría:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                                                categorias.forEach { categoria ->
                                                    CategoriaItemCard(
                                                        categoria = categoria,
                                                        isSelected = categoriaId == categoria.idCategoria,
                                                        onClick = {
                                                        categoriaId = categoria.idCategoria
                                                        categoriaError = false
                                                        expandedCategoria = false
                                                    }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                            Text("No hay categorías disponibles.", color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                                is CategoriasState.Error -> {
                                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                        Text(currentCategoriasState.message, color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
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
                        Text("Género", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AquamarinePrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { expandedGenero = !expandedGenero },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarinePrimary),
                            border = if (generoError) BorderStroke(2.dp, Color.Red) else ButtonDefaults.outlinedButtonBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(20.dp), tint = AquamarinePrimary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = getSelectedGeneroName(generosState, generoId),
                                        fontSize = 14.sp,
                                        fontWeight = if (generoId != null) FontWeight.Medium else FontWeight.Normal,
                                        color = if (generoId != null) AquamarinePrimary else Color.Gray
                                    )
                                }
                                Icon(if (expandedGenero) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AquamarinePrimary)
                            }
                        }

                        if (expandedGenero) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val currentGenerosState = generosState
                            when (currentGenerosState) {
                                is GenerosState.Loading -> {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = AquamarinePrimary)
                                    }
                                }
                                is GenerosState.Success -> {
                                    val generos = currentGenerosState.generos
                                    if (generos.isNotEmpty()) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                                                Text("Selecciona un género:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                                                generos.forEach { genero ->
                                                    GeneroItemCard(
                                                        genero = genero,
                                                        isSelected = generoId == genero.idGenero,
                                                        onClick = {
                                                        generoId = genero.idGenero
                                                        generoError = false
                                                        expandedGenero = false
                                                    }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                            Text("No hay géneros disponibles.", color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                        }
                                    }
                                }
                                is GenerosState.Error -> {
                                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), modifier = Modifier.fillMaxWidth()) {
                                        Text(currentGenerosState.message, color = Color(0xFFD32F2F), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ELIMINAR")
                    }

                    Button(
                        onClick = {
                            nombreModeloError = nombreModelo.isBlank()
                            marcaError = marcaId == null
                            categoriaError = categoriaId == null
                            generoError = generoId == null

                            if (!nombreModeloError && !marcaError && !categoriaError && !generoError) {
                                modeloViewModel.actualizarModelo(
                                    id = modeloId,
                                    nombre = nombreModelo,
                                    marcaId = marcaId!!,
                                    categoriaId = categoriaId!!,
                                    generoId = generoId!!
                                )
                            } else {
                                Toast.makeText(context, "Por favor complete los campos marcados en rojo", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                        enabled = actualizarModeloState !is ActualizarModeloState.Loading
                    ) {
                        if (actualizarModeloState is ActualizarModeloState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ACTUALIZAR")
                        }
                    }
                }
            }
        }
    }
}

// Funciones auxiliares
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