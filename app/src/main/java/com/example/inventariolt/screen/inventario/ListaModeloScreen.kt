package com.example.inventariolt.screen.inventario

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.model.inventario.ModeloResponseDTO
import com.example.inventariolt.viewModel.ModeloListState
import com.example.inventariolt.viewModel.ModeloViewModel
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.viewModel.PerfilState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaModelosScreen(
    navController: NavController,
    userId: Long,
    viewModel: ModeloViewModel = viewModel()
) {
    val state by viewModel.modelosState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarModelos()
    }

    // Filtrar modelos por búsqueda
    val filteredModelos = when (state) {
        is ModeloListState.Success -> {
            val modelos = (state as ModeloListState.Success).modelos
            if (searchQuery.isBlank()) {
                modelos
            } else {
                modelos.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true) ||
                            it.marcaNombre.contains(searchQuery, ignoreCase = true) ||
                            it.categoriaNombre.contains(searchQuery, ignoreCase = true) ||
                            it.generoNombre.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        else -> emptyList()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            Box {
                HeaderConImagen(
                    titulo = "Modelos",
                    subtitulo = "Gestión de modelos",
                    altura = 180.dp
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            // Barra de búsqueda
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre, marca, categoría o género...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = AquamarinePrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = AquamarinePrimary)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AquamarinePrimary,
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state) {
                    is ModeloListState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AquamarinePrimary
                        )
                    }
                    is ModeloListState.Success -> {
                        if (filteredModelos.isEmpty()) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    if (searchQuery.isNotEmpty()) "No se encontraron modelos para \"$searchQuery\""
                                    else "No hay modelos disponibles",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredModelos) { modelo ->
                                    ModeloItem(
                                        modelo = modelo,
                                        onClick = {
                                            navController.navigate("editar_modelo/${modelo.idModelo}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is ModeloListState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                (state as ModeloListState.Error).message,
                                color = Color.Red,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.cargarModelos() },
                                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary)
                            ) {
                                Text("Reintentar")
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
fun ModeloItem(
    modelo: ModeloResponseDTO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(10.dp),
                color = AquamarinePrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = AquamarinePrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información del modelo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modelo.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AquamarineDark
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Badges de información
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AquamarinePrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = modelo.marcaNombre,
                            fontSize = 10.sp,
                            color = AquamarinePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AquamarinePrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = modelo.categoriaNombre,
                            fontSize = 10.sp,
                            color = AquamarinePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AquamarinePrimary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = modelo.generoNombre,
                            fontSize = 10.sp,
                            color = AquamarinePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}