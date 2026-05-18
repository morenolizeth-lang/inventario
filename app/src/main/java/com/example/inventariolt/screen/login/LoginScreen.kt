package com.example.inventariolt.screen.login

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.inventariolt.viewModel.AuthViewModel
import com.example.inventariolt.viewModel.LoginState
import com.example.inventariolt.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Pantalla de login para el sistema de inventario
 * Solo permite acceso a usuarios con rol EMPLEADO
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Estados del formulario
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observar estado del login desde ViewModel
    val loginState by authViewModel.loginState.collectAsState()

    // Animación de entrada
    var startAnimation by remember { mutableStateOf(false) }
    val introAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "intro_alpha"
    )
    val introOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 28.dp,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "intro_offset"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Manejar respuesta del login
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                isLoading = false
                val user = (loginState as LoginState.Success).user

                // Verificar si el usuario tiene rol EMPLEADO
                if (user.rol == "EMPLEADO") {
                    // Guardar datos del usuario en SharedPreferences
                    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().apply {
                        putLong("user_id", user.idUsuario)
                        putString("user_name", user.nombre)
                        putString("user_rol", user.rol)
                        putString("user_password", password) // Guardamos la contraseña real
                        apply()
                    }

                    // Mostrar mensaje de bienvenida
                    scope.launch {
                        snackbarHostState.showSnackbar("✅ ¡Bienvenido ${user.nombre}!")
                    }

                    // Navegar al home
                    navController.navigate("inventario_home/${user.idUsuario}") {
                        popUpTo("login") { inclusive = true }
                    }
                } else {
                    // Usuario no tiene rol EMPLEADO
                    errorMessage = "Esta cuenta no se encuentra registrada como empleado."
                    authViewModel.resetStates()
                }
            }
            is LoginState.Error -> {
                isLoading = false
                errorMessage = (loginState as LoginState.Error).message
                scope.launch {
                    snackbarHostState.showSnackbar(errorMessage ?: "Error al iniciar sesión")
                }
            }
            is LoginState.Loading -> {
                isLoading = true
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AquamarineGradient)
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .alpha(introAlpha)
                    .offset(y = introOffset),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono de inventario
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = AquamarinePrimary.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AllInbox,
                                contentDescription = "Inventario",
                                tint = AquamarinePrimary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Título
                    Text(
                        text = "SISTEMA DE INVENTARIO",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AquamarineDark,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Control de productos y existencias",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Indicador de carga
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = AquamarinePrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Mensaje de error
                    errorMessage?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(),
                            border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = it,
                                    color = Color(0xFFC62828),
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Campo Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                            authViewModel.resetStates()
                        },
                        label = { Text("Correo electrónico") },
                        placeholder = { Text("empleado@ejemplo.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = AquamarinePrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquamarinePrimary,
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                            authViewModel.resetStates()
                        },
                        label = { Text("Contraseña") },
                        placeholder = { Text("Ingrese su contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = AquamarinePrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Mostrar contraseña",
                                    tint = AquamarinePrimary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AquamarinePrimary,
                            unfocusedBorderColor = Color(0xFFBDBDBD),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de inicio de sesión
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Por favor ingrese email y contraseña"
                                return@Button
                            }
                            errorMessage = null
                            authViewModel.login(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AquamarinePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "INICIAR SESIÓN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Línea divisoria con "o"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp
                        )
                        Text(
                            text = " O ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sección de registro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿No tienes una cuenta? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF666666)
                        )
                        Text(
                            text = "Regístrate gratis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AquamarinePrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                authViewModel.resetStates()
                                navController.navigate("registro")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Versión del sistema
                    Text(
                        text = "v1.0.0",
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Preview básica del LoginScreen
 */
@Preview(
    name = "Login Screen - Light Mode",
    showBackground = true,
    backgroundColor = 0xFF00796B
)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        LoginScreen(navController = navController)
    }
}