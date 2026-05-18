package com.example.inventariolt.screen.inventario

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.ui.theme.*
import com.example.inventariolt.model.inventario.VarianteVisualResponseDTO
import com.example.inventariolt.viewModel.VarianteViewModel
import com.example.inventariolt.viewModel.VarianteListState
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.viewModel.PerfilState
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVariantesScreen(
    navController: NavController,
    userId: Long,
    viewModel: VarianteViewModel = viewModel()
) {
    val state by viewModel.variantesState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarVariantes()
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
                    titulo = "Variantes",
                    subtitulo = "Opciones de producto",
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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state) {
                    is VarianteListState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = AquamarinePrimary
                        )
                    }
                    is VarianteListState.Success -> {
                        val variantes = (state as VarianteListState.Success).variantes
                        if (variantes.isEmpty()) {
                            Text(
                                "No hay variantes disponibles",
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.Gray
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(variantes) { variante ->
                                    VarianteItem(
                                        variante = variante,
                                        onToggleStatus = { viewModel.cambiarEstado(variante) },
                                        onClick = {
                                            val variantJson = Uri.encode(Gson().toJson(variante))
                                            navController.navigate("detalle_variante/$variantJson")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is VarianteListState.Error -> {
                        Text(
                            (state as VarianteListState.Error).message,
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Red
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun VarianteItem(
    variante: VarianteVisualResponseDTO,
    onToggleStatus: () -> Unit,
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
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Surface(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFFF0F0F0)
            ) {
                if (variante.imagen != null) {
                    SubcomposeAsyncImage(
                        model = variante.imagen,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        error = { Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray) }
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.padding(16.dp), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = variante.modeloNombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${variante.colorPrimarioNombre}${if (variante.colorSecundarioNombre != null) " / " + variante.colorSecundarioNombre else ""}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
