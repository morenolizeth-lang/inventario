package com.example.inventariolt.screen.inventario

import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.inventariolt.model.inventario.ModeloResponseDTO
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.ColoresState
import com.example.inventariolt.viewModel.CrearVarianteState
import com.example.inventariolt.viewModel.ModelosState
import com.example.inventariolt.viewModel.SubirImagenState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearVarianteScreen(
    navController: NavController,
    viewModel: AgregarProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para crear nueva variante
    var modeloSeleccionadoId by remember { mutableStateOf<Long?>(null) }
    var modeloSeleccionado by remember { mutableStateOf<ModeloResponseDTO?>(null) }
    var expandedModelos by remember { mutableStateOf(false) }
    var nuevoColorPrimarioId by remember { mutableStateOf<Long?>(null) }
    var nuevoColorSecundarioId by remember { mutableStateOf<Long?>(null) }
    var imagenSeleccionadaUri by remember { mutableStateOf<Uri?>(null) }
    var varianteCreadaId by remember { mutableStateOf<Long?>(null) }

    // Estados de expansión para colores
    var expandedColorPrimario by remember { mutableStateOf(false) }
    var expandedColorSecundario by remember { mutableStateOf(false) }

    // Estados del ViewModel
    val coloresState by viewModel.coloresState.collectAsState()
    val modelosState by viewModel.modelosState.collectAsState()
    val crearVarianteState by viewModel.crearVarianteState.collectAsState()
    val subirImagenState by viewModel.subirImagenState.collectAsState()

    // Cargar datos iniciales
    LaunchedEffect(Unit) {
        viewModel.cargarDatosIniciales()
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

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Crear Variante",
                    subtitulo = "Nueva opción visual",
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
            // Selector de Modelo
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Modelo",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón desplegable
                    OutlinedButton(
                        onClick = { expandedModelos = !expandedModelos },
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
                                    Icons.Default.Style,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = modeloSeleccionado?.nombre ?: "Seleccione un modelo",
                                        fontSize = 14.sp,
                                        fontWeight = if (modeloSeleccionado != null) FontWeight.Medium else FontWeight.Normal,
                                        color = if (modeloSeleccionado != null) AquamarinePrimary else Color.Gray
                                    )
                                    if (modeloSeleccionado != null) {
                                        Text(
                                            text = "${modeloSeleccionado!!.marcaNombre} • ${modeloSeleccionado!!.categoriaNombre} • ${modeloSeleccionado!!.generoNombre}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                            Icon(
                                if (expandedModelos) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (expandedModelos) "Contraer" else "Expandir",
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    // Lista desplegable de modelos
                    if (expandedModelos) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val modelosStateValue = modelosState
                        when (modelosStateValue) {
                            is ModelosState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is ModelosState.Success -> {
                                val modelos = modelosStateValue.modelos
                                if (modelos.isNotEmpty()) {
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
                                                text = "Selecciona un modelo:",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            modelos.forEach { modelo ->
                                                ModeloCard(
                                                    modelo = modelo,
                                                    isSelected = modeloSeleccionadoId == modelo.idModelo,
                                                    onClick = {
                                                        modeloSeleccionadoId = modelo.idModelo
                                                        modeloSeleccionado = modelo
                                                        expandedModelos = false
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
                                            text = "No hay modelos disponibles. Crea uno nuevo.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is ModelosState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar modelos",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                            else -> {}
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            navController.navigate("crear_modelo")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear Nuevo Modelo")
                    }
                }
            }

            // Selector de Color Primario
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Color Primario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { expandedColorPrimario = !expandedColorPrimario },
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
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                val coloresStateValue = coloresState
                                Text(
                                    text = when (coloresStateValue) {
                                        is ColoresState.Success -> coloresStateValue.colores.find { it.idColor == nuevoColorPrimarioId }?.nombre ?: "Seleccionar Color"
                                        else -> "Cargando..."
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (nuevoColorPrimarioId != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (nuevoColorPrimarioId != null) AquamarinePrimary else Color.Gray
                                )
                            }
                            Icon(
                                if (expandedColorPrimario) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    if (expandedColorPrimario) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val coloresStateValue = coloresState
                        when (coloresStateValue) {
                            is ColoresState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is ColoresState.Success -> {
                                val colores = coloresStateValue.colores
                                if (colores.isNotEmpty()) {
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
                                                text = "Selecciona un color:",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            colores.forEach { color ->
                                                ColorItemCard(
                                                    color = color,
                                                    isSelected = nuevoColorPrimarioId == color.idColor,
                                                    onClick = {
                                                        nuevoColorPrimarioId = color.idColor
                                                        expandedColorPrimario = false
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
                                            text = "No hay colores disponibles.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is ColoresState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar colores",
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

            // Selector de Color Secundario (Opcional)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Color Secundario (Opcional)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { expandedColorSecundario = !expandedColorSecundario },
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
                                    Icons.Default.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = AquamarinePrimary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                val coloresStateValue = coloresState
                                Text(
                                    text = when (coloresStateValue) {
                                        is ColoresState.Success -> coloresStateValue.colores.find { it.idColor == nuevoColorSecundarioId }?.nombre ?: "Seleccionar Color (Opcional)"
                                        else -> "Cargando..."
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (nuevoColorSecundarioId != null) FontWeight.Medium else FontWeight.Normal,
                                    color = if (nuevoColorSecundarioId != null) AquamarinePrimary else Color.Gray
                                )
                            }
                            Icon(
                                if (expandedColorSecundario) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = AquamarinePrimary
                            )
                        }
                    }

                    if (expandedColorSecundario) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val coloresStateValue = coloresState
                        when (coloresStateValue) {
                            is ColoresState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = AquamarinePrimary)
                                }
                            }
                            is ColoresState.Success -> {
                                val colores = coloresStateValue.colores
                                if (colores.isNotEmpty()) {
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
                                                text = "Selecciona un color (opcional):",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                                            )
                                            colores.forEach { color ->
                                                ColorItemCard(
                                                    color = color,
                                                    isSelected = nuevoColorSecundarioId == color.idColor,
                                                    onClick = {
                                                        nuevoColorSecundarioId = color.idColor
                                                        expandedColorSecundario = false
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
                                            text = "No hay colores disponibles.",
                                            color = Color(0xFFD32F2F),
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                }
                            }
                            is ColoresState.Error -> {
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Error al cargar colores",
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

            // Selector de imagen
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Imagen de la variante",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AquamarinePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { imagenLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (modeloSeleccionadoId != null && nuevoColorPrimarioId != null) {
                        viewModel.crearVariante(
                            modeloId = modeloSeleccionadoId!!,
                            colorPrimarioId = nuevoColorPrimarioId!!,
                            colorSecundarioId = nuevoColorSecundarioId
                        )
                    } else {
                        Toast.makeText(context, "Selecciona un modelo y color primario", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                enabled = crearVarianteState !is CrearVarianteState.Loading
            ) {
                if (crearVarianteState is CrearVarianteState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("CREAR VARIANTE", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ModeloCard(
    modelo: ModeloResponseDTO,
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
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = AquamarinePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = modelo.nombre,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = AquamarineDark,
                    maxLines = 1
                )
                Text(
                    text = "Marca: ${modelo.marcaNombre}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Categoría: ${modelo.categoriaNombre}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Género: ${modelo.generoNombre}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

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
fun ColorItemCard(
    color: com.example.inventariolt.model.inventario.ColorResponseDTO,
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
                        Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = AquamarinePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = color.nombre,
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