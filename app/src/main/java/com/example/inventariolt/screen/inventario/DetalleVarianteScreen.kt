package com.example.inventariolt.screen.inventario

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualRequestDTO
import com.example.inventariolt.model.inventario_Empleado.VarianteVisualResponseDTO
import com.example.inventariolt.viewModel.AgregarProductoViewModel
import com.example.inventariolt.viewModel.ColoresState
import com.example.inventariolt.viewModel.ModelosState
import com.example.inventariolt.viewModel.OperacionState
import com.example.inventariolt.viewModel.SubirImagenState
import com.example.inventariolt.viewModel.VarianteViewModel
import com.example.inventariolt.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleVarianteScreen(
    navController: NavController,
    variante: VarianteVisualResponseDTO,
    viewModel: VarianteViewModel = viewModel(),
    agregarViewModel: AgregarProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val operacionState by viewModel.operacionState.collectAsState()
    val coloresState by agregarViewModel.coloresState.collectAsState()
    val modelosState by agregarViewModel.modelosState.collectAsState()
    val subirImagenState by agregarViewModel.subirImagenState.collectAsState()
    
    var modeloId by remember { mutableLongStateOf(variante.modeloId) }
    var colorPrimarioId by remember { mutableLongStateOf(variante.colorPrimarioId) }
    var colorSecundarioId by remember { mutableStateOf(variante.colorSecundarioId) }
    var imagenSeleccionadaUri by remember { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val imagenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagenSeleccionadaUri = uri
    }

    LaunchedEffect(Unit) {
        agregarViewModel.cargarColores()
        agregarViewModel.cargarModelos()
    }

    LaunchedEffect(operacionState) {
        when (operacionState) {
            is OperacionState.Success -> {
                if (imagenSeleccionadaUri != null) {
                    val file = FileUtils.uriToFile(context, imagenSeleccionadaUri!!)
                    if (file != null) {
                        agregarViewModel.subirImagenVariante(variante.idVarianteVisual, file)
                    }
                } else {
                    Toast.makeText(context, (operacionState as OperacionState.Success).message, Toast.LENGTH_SHORT).show()
                    viewModel.resetOperacionState()
                    navController.popBackStack()
                }
            }
            is OperacionState.Error -> {
                Toast.makeText(context, (operacionState as OperacionState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetOperacionState()
            }
            else -> {}
        }
    }

    LaunchedEffect(subirImagenState) {
        when (subirImagenState) {
            is SubirImagenState.Success -> {
                Toast.makeText(context, "Variante e imagen actualizadas", Toast.LENGTH_SHORT).show()
                viewModel.resetOperacionState()
                navController.popBackStack()
            }
            is SubirImagenState.Error -> {
                Toast.makeText(context, (subirImagenState as SubirImagenState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Variante") },
            text = { Text("¿Estás seguro de que deseas eliminar esta variante? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.eliminarVariante(variante.idVarianteVisual)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
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
                    titulo = "Detalle de Variante",
                    subtitulo = "Visualiza y edita esta opción",
                    altura = 180.dp
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

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .offset(y = (-20).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen de la variante
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier.size(200.dp)
                ) {
                    if (imagenSeleccionadaUri != null) {
                        AsyncImage(
                            model = imagenSeleccionadaUri,
                            contentDescription = "Nueva imagen",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else if (variante.imagen != null) {
                        SubcomposeAsyncImage(
                            model = variante.imagen,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            error = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray) }
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray)
                        }
                    }
                }

                Button(
                    onClick = { imagenLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cambiar Imagen", fontSize = 14.sp)
                }

                // Información
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Información General", fontWeight = FontWeight.Bold, color = AquamarineDark)
                        HorizontalDivider(color = AquamarineLight)
                        
                        ModeloSelectorEdit(
                            modelosState = modelosState,
                            selectedModeloId = modeloId,
                            onModeloSelected = { modeloId = it }
                        )

                        ColorSelectorEdit(
                            coloresState = coloresState,
                            selectedColorId = colorPrimarioId,
                            label = "Color Primario",
                            onColorSelected = { if (it != null) colorPrimarioId = it }
                        )

                        ColorSelectorEdit(
                            coloresState = coloresState,
                            selectedColorId = colorSecundarioId,
                            label = "Color Secundario (Opcional)",
                            onColorSelected = { colorSecundarioId = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones de acción
                Button(
                    onClick = {
                        val request = VarianteVisualRequestDTO(
                            modeloId = modeloId,
                            colorPrimarioId = colorPrimarioId,
                            colorSecundarioId = colorSecundarioId,
                            imagen = variante.imagen,
                            estado = true // Siempre activo al guardar cambios
                        )
                        viewModel.actualizarVariante(variante.idVarianteVisual, request)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(AquamarineGradient, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    enabled = operacionState !is OperacionState.Loading && subirImagenState !is SubirImagenState.Loading
                ) {
                    if (operacionState is OperacionState.Loading || subirImagenState is SubirImagenState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ELIMINAR VARIANTE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ModeloSelectorEdit(
    modelosState: ModelosState,
    selectedModeloId: Long?,
    onModeloSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("Modelo", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (modelosState) {
                        is ModelosState.Success -> modelosState.modelos.find { it.idModelo == selectedModeloId }?.nombre ?: "Seleccionar"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 4.dp).background(Color.White, RoundedCornerShape(8.dp)).verticalScroll(rememberScrollState())) {
                Column {
                    if (modelosState is ModelosState.Success) {
                        modelosState.modelos.forEach { modelo ->
                            Text(text = modelo.nombre, modifier = Modifier.fillMaxWidth().clickable { onModeloSelected(modelo.idModelo); expanded = false }.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSelectorEdit(
    coloresState: ColoresState,
    selectedColorId: Long?,
    label: String,
    onColorSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = when (coloresState) {
                        is ColoresState.Success -> coloresState.colores.find { it.idColor == selectedColorId }?.nombre ?: "Seleccionar"
                        else -> "Cargando..."
                    },
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
            }
        }
        if (expanded) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).padding(top = 4.dp).background(Color.White, RoundedCornerShape(8.dp)).verticalScroll(rememberScrollState())) {
                Column {
                    if (label.contains("Opcional")) {
                        Text(text = "Ninguno", color = Color.Red, modifier = Modifier.fillMaxWidth().clickable { onColorSelected(null); expanded = false }.padding(12.dp))
                    }
                    if (coloresState is ColoresState.Success) {
                        coloresState.colores.forEach { color ->
                            Text(text = color.nombre, modifier = Modifier.fillMaxWidth().clickable { onColorSelected(color.idColor); expanded = false }.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}
