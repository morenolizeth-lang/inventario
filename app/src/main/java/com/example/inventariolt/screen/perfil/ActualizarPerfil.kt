package com.example.inventariolt.screen.perfil

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarPerfilScreen(
    navController: NavController,
    userId: Long,
    viewModel: UsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE) }
    val currentSavedPassword = remember { prefs.getString("user_password", "") ?: "" }
    val perfilState by viewModel.perfilState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var cambiarPassword by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Nuevo estado para la imagen seleccionada
    var showSolicitudDialog by remember { mutableStateOf(false) }
    var mensajeSolicitud by remember { mutableStateOf("") }

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
                if (cambiarPassword && password.isNotEmpty()) {
                    prefs.edit().putString("user_password", password).apply()
                }
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

    if (showSolicitudDialog && perfilState is PerfilState.Success) {
        val usuario = (perfilState as PerfilState.Success).usuario
        AlertDialog(
            onDismissRequest = { showSolicitudDialog = false },
            title = { Text("Solicitar cambio a Administrador", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Explica brevemente por qué solicitas el cambio de rol. Se abrirá tu aplicación de correo para enviar la solicitud.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = mensajeSolicitud,
                        onValueChange = { mensajeSolicitud = it },
                        label = { Text("Motivo de la solicitud") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (mensajeSolicitud.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf("admin_inventario@empresa.com")) // Cambia por el correo real
                                putExtra(Intent.EXTRA_SUBJECT, "Solicitud de Cambio de Rol - ${usuario.nombre}")
                                putExtra(Intent.EXTRA_TEXT, """
                                    SOLICITUD DE CAMBIO DE ROL
                                    
                                    Usuario: ${usuario.nombre}
                                    ID: $userId
                                    Correo: ${usuario.correo}
                                    Rol Actual: ${usuario.rol}
                                    
                                    Motivo de la solicitud:
                                    $mensajeSolicitud
                                    
                                    Enviado desde InventarioLT App.
                                """.trimIndent())
                            }
                            try {
                                context.startActivity(Intent.createChooser(intent, "Enviar solicitud por:"))
                                showSolicitudDialog = false
                                mensajeSolicitud = ""
                            } catch (e: Exception) {
                                Toast.makeText(context, "No tienes aplicaciones de correo configuradas", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Por favor, ingresa un motivo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary)
                ) {
                    Text("Enviar Solicitud")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSolicitudDialog = false }) { Text("Cancelar") }
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it // Solo guardamos la URI localmente
        }
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when (perfilState) {
                is PerfilState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AquamarinePrimary
                    )
                }

                is PerfilState.Success -> {
                    val usuario = (perfilState as PerfilState.Success).usuario

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header con foto superpuesta (Estilo unificado)
                        Box(contentAlignment = Alignment.BottomCenter) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.TopStart
                            ) {
                                HeaderConImagen(
                                    titulo = "Editar Perfil",
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

                            // Foto de perfil superpuesta
                            Box(
                                modifier = Modifier.offset(y = 50.dp),
                                contentAlignment = Alignment.BottomEnd
                            ) {
                                Surface(
                                    modifier = Modifier.size(110.dp),
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 8.dp
                                ) {
                                    if (imageUri != null) {
                                        // Mostrar la imagen seleccionada localmente si existe
                                        SubcomposeAsyncImage(
                                            model = imageUri,
                                            contentDescription = "Nueva foto de perfil",
                                            modifier = Modifier.clip(CircleShape),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else if (usuario.fotoPerfil == null) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.padding(20.dp),
                                            tint = Color.Gray
                                        )
                                    } else {
                                        SubcomposeAsyncImage(
                                            model = usuario.fotoPerfil,
                                            contentDescription = "Foto de perfil actual",
                                            modifier = Modifier.clip(CircleShape),
                                            contentScale = ContentScale.Fit
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
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Cambiar foto",
                                        modifier = Modifier.size(18.dp)
                                    )
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

                            // Mostrar Rol (Lectura solamente)
                            OutlinedTextField(
                                value = when (usuario.rol) {
                                    "CONSULTA" -> "EMPLEADO"
                                    "EMPLEADO" -> "ADMIN TIENDA"
                                    else -> usuario.rol
                                },
                                onValueChange = { },
                                label = { Text("Rol") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Badge,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                readOnly = true,
                                enabled = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color.LightGray,
                                    disabledTextColor = Color.Gray,
                                    disabledLabelColor = Color.Gray,
                                    disabledLeadingIconColor = Color.Gray
                                )
                            )

                            OutlinedTextField(
                                value = nombre,
                                onValueChange = { nombre = it },
                                label = { Text("Nombre Completo") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null
                                    )
                                },
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
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null
                                    )
                                },
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
                                    modifier = Modifier.clickable {
                                        cambiarPassword = !cambiarPassword
                                    }
                                )
                            }

                            // Campos de contraseña solo si se seleccionó el checkbox
                            if (cambiarPassword) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Nueva Contraseña") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            isPasswordVisible = !isPasswordVisible
                                        }) {
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
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            isConfirmPasswordVisible = !isConfirmPasswordVisible
                                        }) {
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
                                    val finalPassword = if (cambiarPassword && password.isNotEmpty()) {
                                        password
                                    } else {
                                        currentSavedPassword
                                    }

                                    val request = UsuarioRequestDTO(
                                        nombre = nombre.trim(),
                                        correo = correo.trim(),
                                        password = finalPassword,
                                        rol = usuario.rol,
                                        estado = usuario.estado,
                                        tiendaId = usuario.tiendaId
                                    )

                                    var body: MultipartBody.Part? = null
                                    imageUri?.let { uri ->
                                        val file = com.example.inventariolt.utils.FileUtils.uriToFile(context, uri)
                                        file?.let {
                                            val requestFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                                            body = MultipartBody.Part.createFormData("file", it.name, requestFile)
                                        }
                                    }

                                    viewModel.actualizarPerfilCompleto(userId, request, body)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary),
                                enabled = updateState !is UpdatePerfilState.Loading && canSave
                            ) {
                                if (updateState is UpdatePerfilState.Loading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        "Guardar Cambios",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Botón para solicitar cambio de rol (Solo para CONSULTA)
                            if (usuario.rol == "CONSULTA") {
                                OutlinedButton(
                                    onClick = { showSolicitudDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, AquamarinePrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AquamarinePrimary)
                                ) {
                                    Icon(Icons.Default.Upgrade, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Solicitar ser Administrador",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                is PerfilState.Error -> {
                    Text(
                        "Error: ${(perfilState as PerfilState.Error).message}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {}
            }
        }
    }
}
