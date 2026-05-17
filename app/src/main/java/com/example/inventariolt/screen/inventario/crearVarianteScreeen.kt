package com.example.inventariolt.screen.inventario

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import com.example.inventariolt.utils.FileUtils
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Brush
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.CategoriasState
import com.example.inventariolt.viewModel.ColoresState
import com.example.inventariolt.viewModel.CrearModeloState
import com.example.inventariolt.viewModel.CrearVarianteState
import com.example.inventariolt.viewModel.GenerosState
import com.example.inventariolt.viewModel.MarcasState
import com.example.inventariolt.viewModel.ModelosState
import com.example.inventariolt.viewModel.SubirImagenState
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearVarianteScreen(
    navController: NavController,
    viewModel: AgregarProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para crear nueva variante
    var nuevoModeloNombre by remember { mutableStateOf("") }
    var nuevaMarcaId by remember { mutableStateOf<Long?>(null) }
    var nuevaCategoriaId by remember { mutableStateOf<Long?>(null) }
    var nuevoGeneroId by remember { mutableStateOf<Long?>(null) }
    var nuevoColorPrimarioId by remember { mutableStateOf<Long?>(null) }
    var nuevoColorSecundarioId by remember { mutableStateOf<Long?>(null) }
    var imagenSeleccionadaUri by remember { mutableStateOf<Uri?>(null) }
    var varianteCreadaId by remember { mutableStateOf<Long?>(null) }
    var modeloId by remember { mutableStateOf(0L) }
    var estaCreandoModelo by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val categoriasState by viewModel.categoriasState.collectAsState()
    val marcasState by viewModel.marcasState.collectAsState()
    val generosState by viewModel.generosState.collectAsState()
    val coloresState by viewModel.coloresState.collectAsState()
    val modelosState by viewModel.modelosState.collectAsState()
    val crearModeloState by viewModel.crearModeloState.collectAsState()
    val crearVarianteState by viewModel.crearVarianteState.collectAsState()
    val subirImagenState by viewModel.subirImagenState.collectAsState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
    }

    // Observar creación de modelo
    LaunchedEffect(crearModeloState) {
        when (crearModeloState) {
            is CrearModeloState.Success -> {
                val modelo = (crearModeloState as CrearModeloState.Success).modelo
                modeloId = modelo.idModelo
                estaCreandoModelo = false
                // Crear variante con el ID del modelo obtenido
                viewModel.crearVariante(
                    modeloId = modelo.idModelo,
                    colorPrimarioId = nuevoColorPrimarioId!!,
                    colorSecundarioId = nuevoColorSecundarioId
                )
            }
            is CrearModeloState.Error -> {
                estaCreandoModelo = false
                Toast.makeText(context, (crearModeloState as CrearModeloState.Error).message, Toast.LENGTH_LONG).show()
            }
            is CrearModeloState.Loading -> {
                estaCreandoModelo = true
            }
            else -> {}
        }
    }

    // Manejar creación de variante
    LaunchedEffect(crearVarianteState) {
        when (crearVarianteState) {
            is CrearVarianteState.Success -> {
                val variante = (crearVarianteState as CrearVarianteState.Success).variante
                varianteCreadaId = variante.idVarianteVisual
                if (imagenSeleccionadaUri != null) {
                    val file = FileUtils.uriToFile(context, imagenSeleccionadaUri!!)
                    file?.let { viewModel.subirImagenVariante(varianteCreadaId!!, it) }
                } else {
                    Toast.makeText(context, "Variante creada exitosamente", Toast.LENGTH_SHORT).show()
                    viewModel.cargarVariantes()
                    navController.popBackStack()
                }
            }
            is CrearVarianteState.Error -> {
                Toast.makeText(context, (crearVarianteState as CrearVarianteState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    // Manejar subida de imagen
    LaunchedEffect(subirImagenState) {
        when (subirImagenState) {
            is SubirImagenState.Success -> {
                Toast.makeText(context, "Variante creada con imagen exitosamente", Toast.LENGTH_SHORT).show()
                viewModel.cargarVariantes()
                navController.popBackStack()
            }
            is SubirImagenState.Error -> {
                Toast.makeText(context, (subirImagenState as SubirImagenState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val imagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionadaUri = uri
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                HeaderConImagen(
                    titulo = "Crear Nueva Variante",
                    subtitulo = "Configura una nueva opción visual",
                    altura = 160.dp
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Información de la Variante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AquamarineDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    MarcaSelector(
                        marcasState = marcasState,
                        selectedMarcaId = nuevaMarcaId,
                        onMarcaSelected = { nuevaMarcaId = it }
                    )

                    CategoriaSelector(
                        categoriasState = categoriasState,
                        selectedCategoriaId = nuevaCategoriaId,
                        onCategoriaSelected = { nuevaCategoriaId = it }
                    )

                    GeneroSelector(
                        generosState = generosState,
                        selectedGeneroId = nuevoGeneroId,
                        onGeneroSelected = { nuevoGeneroId = it }
                    )

                    ModeloSelector(
                        modelosState = modelosState,
                        selectedModeloId = if(modeloId == 0L) null else modeloId,
                        onModeloSelected = { modeloId = it }
                    )

                    ColorSelector(
                        coloresState = coloresState,
                        selectedColorId = nuevoColorPrimarioId,
                        label = "Color Primario",
                        onColorSelected = { nuevoColorPrimarioId = it }
                    )

                    ColorSelector(
                        coloresState = coloresState,
                        selectedColorId = nuevoColorSecundarioId,
                        label = "Color Secundario (Opcional)",
                        onColorSelected = { nuevoColorSecundarioId = it }
                    )

                    // Selector de imagen
                    Column {
                        Text("Imagen de la variante", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { imagenLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E))
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (imagenSeleccionadaUri != null) "Cambiar imagen" else "Seleccionar imagen")
                        }
                        if (imagenSeleccionadaUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AsyncImage(
                                model = imagenSeleccionadaUri,
                                contentDescription = "Vista previa",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (modeloId != 0L && nuevoColorPrimarioId != null) {
                                viewModel.crearVariante(
                                    modeloId = modeloId,
                                    colorPrimarioId = nuevoColorPrimarioId!!,
                                    colorSecundarioId = nuevoColorSecundarioId
                                )
                            } else {
                                Toast.makeText(context, "Selecciona un modelo y color primario", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(AquamarineGradient, RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !estaCreandoModelo && crearVarianteState !is CrearVarianteState.Loading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        when {
                            estaCreandoModelo -> {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creando modelo...", color = Color.White)
                            }
                            crearVarianteState is CrearVarianteState.Loading -> {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Creando variante...", color = Color.White)
                            }
                            else -> Text("CREAR VARIANTE", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==================== COMPONENTES AUXILIARES ====================

@Composable
fun MarcaSelector(
    marcasState: MarcasState,
    selectedMarcaId: Long?,
    onMarcaSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Marca *", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (marcasState) {
                        is MarcasState.Success -> marcasState.marcas.find { it.idMarca == selectedMarcaId }?.nombre ?: "Seleccionar Marca"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    when (marcasState) {
                        is MarcasState.Success -> {
                            marcasState.marcas.forEach { marca ->
                                Text(
                                    text = marca.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onMarcaSelected(marca.idMarca)
                                            expanded = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                        else -> { Text("Cargando...", modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriaSelector(
    categoriasState: CategoriasState,
    selectedCategoriaId: Long?,
    onCategoriaSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Categoría *", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (categoriasState) {
                        is CategoriasState.Success -> categoriasState.categorias.find { it.idCategoria == selectedCategoriaId }?.nombre ?: "Seleccionar Categoría"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    when (categoriasState) {
                        is CategoriasState.Success -> {
                            categoriasState.categorias.forEach { categoria ->
                                Text(
                                    text = categoria.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onCategoriaSelected(categoria.idCategoria)
                                            expanded = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                        else -> { Text("Cargando...", modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun GeneroSelector(
    generosState: GenerosState,
    selectedGeneroId: Long?,
    onGeneroSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Género *", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (generosState) {
                        is GenerosState.Success -> generosState.generos.find { it.idGenero == selectedGeneroId }?.nombre ?: "Seleccionar Género"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    when (generosState) {
                        is GenerosState.Success -> {
                            generosState.generos.forEach { genero ->
                                Text(
                                    text = genero.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onGeneroSelected(genero.idGenero)
                                            expanded = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                        else -> { Text("Cargando...", modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeloSelector(
    modelosState: ModelosState,
    selectedModeloId: Long?,
    onModeloSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Modelo *", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (modelosState) {
                        is ModelosState.Success -> modelosState.modelos.find { it.idModelo == selectedModeloId }?.nombre ?: "Seleccionar Modelo"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    when (modelosState) {
                        is ModelosState.Success -> {
                            modelosState.modelos.forEach { modelo ->
                                Text(
                                    text = modelo.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onModeloSelected(modelo.idModelo)
                                            expanded = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                        else -> { Text("Cargando...", modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSelector(
    coloresState: ColoresState,
    selectedColorId: Long?,
    label: String,
    onColorSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (coloresState) {
                        is ColoresState.Success -> coloresState.colores.find { it.idColor == selectedColorId }?.nombre ?: "Seleccionar Color"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                Column {
                    when (coloresState) {
                        is ColoresState.Success -> {
                            coloresState.colores.forEach { color ->
                                Text(
                                    text = color.nombre,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onColorSelected(color.idColor)
                                            expanded = false
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                        else -> { Text("Cargando...", modifier = Modifier.padding(12.dp)) }
                    }
                }
            }
        }
    }
}


// Función auxiliar para convertir Uri a File
