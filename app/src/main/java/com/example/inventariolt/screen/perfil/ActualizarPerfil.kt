package com.example.inventariolt.screen.perfil

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.inventariolt.model.login.UsuarioRequestDTO
import com.example.inventariolt.viewModel.PerfilState
import com.example.inventariolt.viewModel.UpdatePerfilState
import com.example.inventariolt.viewModel.UsuarioViewModel
import com.example.inventariolt.ui.theme.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarPerfilScreen(
    navController: NavController,
    userId: Long,
    viewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val perfilState by viewModel.perfilState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var cambiarPassword by remember { mutableStateOf(false) } // Nuevo estado

    // Cargar datos iniciales
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)
    }

    // Sincronizar campos cuando el perfil se carga
    LaunchedEffect(perfilState) {
        if (perfilState is PerfilState.Success) {
            val usuario = (perfilState as PerfilState.Success).usuario
            nombre = usuario.nombre
            correo = usuario.correo
        }
    }

    // Manejar estados de actualización
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdatePerfilState.Success -> {
                Toast.makeText(context, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetUpdateState()
                navController.popBackStack()
            }
            is UpdatePerfilState.Error -> {
                Toast.makeText(context, (updateState as UpdatePerfilState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                viewModel.subirFotoPerfil(userId, body)
            }
        }
    }

    Scaffold(
        topBar = {
            Box {
                HeaderConImagen(
                    titulo = "Editar Perfil",
                    altura = 120.dp
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (perfilState) {
                is PerfilState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AquamarinePrimary)
                }
                is PerfilState.Success -> {
                    val usuario = (perfilState as PerfilState.Success).usuario

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header con foto y opción de editar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AquamarineGradient)
                            )
                            Box(
                                modifier = Modifier.offset(y = 60.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    modifier = Modifier.size(110.dp),
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 4.dp
                                ) {
                                    if (usuario.fotoPerfil == null) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.padding(20.dp),
                                            tint = Color.Gray
                                        )
                                    } else {
                                        SubcomposeAsyncImage(
                                            model = usuario.fotoPerfil,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier.clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                FloatingActionButton(
                                    onClick = { launcher.launch("image/*") },
                                    modifier = Modifier.size(36.dp),
                                    shape = CircleShape,
                                    containerColor = Color.White,
                                    contentColor = AquamarinePrimary
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "Cambiar foto", modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(60.dp))

                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Información del Usuario",
                                style = MaterialTheme.typography.titleMedium,
                                color = AquamarinePrimary,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre Completo") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AquamarinePrimary,
                                    focusedLabelColor = AquamarinePrimary,
                                    focusedLeadingIconColor = AquamarinePrimary
                                )
                            )

                            OutlinedTextField(
                                value = correo,
                                onValueChange = { correo = it },
                                label = { Text("Correo Electrónico") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AquamarinePrimary,
                                    focusedLabelColor = AquamarinePrimary,
                                    focusedLeadingIconColor = AquamarinePrimary
                                )
                            )

                            // Checkbox para habilitar cambio de contraseña
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = cambiarPassword,
                                    onCheckedChange = { cambiarPassword = it },
                                    colors = CheckboxDefaults.colors(checkedColor = AquamarinePrimary)
                                )
                                Text(
                                    text = "Cambiar contraseña",
                                    fontSize = 14.sp,
                                    color = AquamarineDark,
                                    modifier = Modifier.clickable { cambiarPassword = !cambiarPassword }
                                )
                            }

                            // Campos de contraseña solo si se seleccionó el checkbox
                            if (cambiarPassword) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Nueva Contraseña") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Mostrar contraseña"
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AquamarinePrimary,
                                        focusedLabelColor = AquamarinePrimary,
                                        focusedLeadingIconColor = AquamarinePrimary
                                    )
                                )

                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Verificar Contraseña") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Mostrar contraseña"
                                            )
                                        }
                                    },
                                    isError = password != confirmPassword && confirmPassword.isNotEmpty(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AquamarinePrimary,
                                        focusedLabelColor = AquamarinePrimary,
                                        focusedLeadingIconColor = AquamarinePrimary
                                    )
                                )

                                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                                    Text(
                                        text = "Las contraseñas no coinciden",
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Validación para guardar
                            val canSave = nombre.isNotEmpty() &&
                                    correo.isNotEmpty() &&
                                    (!cambiarPassword || (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword))

                            Button(
                                onClick = {
                                    val request = UsuarioRequestDTO(
                                        nombre = nombre,
                                        correo = correo,
                                        password = if (cambiarPassword) password else "", // Si no cambia contraseña, enviar vacío o null
                                        rol = usuario.rol,
                                        estado = usuario.estado,
                                        tiendaId = usuario.tiendaId
                                    )
                                    viewModel.actualizarPerfil(userId, request)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                                enabled = updateState !is UpdatePerfilState.Loading && canSave
                            ) {
                                if (updateState is UpdatePerfilState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is PerfilState.Error -> {
                    Text("Error: ${(perfilState as PerfilState.Error).message}", modifier = Modifier.align(Alignment.Center))
                }
                else -> {}
            }
        }
    }
}

// Función auxiliar para convertir Uri a File (necesaria para Multipart)
fun uriToFile(context: android.content.Context, uri: Uri): File? {
    val contentResolver = context.contentResolver
    val fileName = getFileName(context, uri)
    val tempFile = File(context.cacheDir, fileName)

    try {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun getFileName(context: android.content.Context, uri: Uri): String {
    var name = "temp_image"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}