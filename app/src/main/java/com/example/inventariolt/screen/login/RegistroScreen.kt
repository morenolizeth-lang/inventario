package com.example.inventariolt.screen.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.inventariolt.viewModel.AuthViewModel
import com.example.inventariolt.viewModel.RegisterState
import com.example.inventariolt.viewModel.SendCodeState
import com.example.inventariolt.viewModel.VerifyCodeState
import com.example.inventariolt.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Validación de contraseña con requisitos de seguridad
fun isValidPassword(password: String): Pair<Boolean, String> {
    if (password.isBlank()) {
        return Pair(false, "Por favor ingrese una contraseña")
    }

    if (password.length < 8) {
        return Pair(false, "La contraseña debe tener al menos 8 caracteres")
    }

    // Verificar al menos una letra mayúscula
    if (!password.any { it.isUpperCase() }) {
        return Pair(false, "La contraseña debe contener al menos una letra mayúscula")
    }

    // Verificar al menos una letra minúscula
    if (!password.any { it.isLowerCase() }) {
        return Pair(false, "La contraseña debe contener al menos una letra minúscula")
    }

    // Verificar al menos un número
    if (!password.any { it.isDigit() }) {
        return Pair(false, "La contraseña debe contener al menos un número")
    }

    // Verificar al menos un carácter especial
    val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?/~`"
    if (!password.any { it in specialChars }) {
        return Pair(false, "La contraseña debe contener al menos un carácter especial (!@#$%^&*)")
    }

    // Verificar que no tenga espacios
    if (password.contains(" ")) {
        return Pair(false, "La contraseña no debe contener espacios")
    }

    return Pair(true, "")
}

@Composable
fun RegistroScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var inputCode by remember { mutableStateOf("") }
    var codeError by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val sendCodeState by authViewModel.sendCodeState.collectAsState()
    val verifyCodeState by authViewModel.verifyCodeState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()
    val context = LocalContext.current

    // Lista de dominios válidos
    val validDomains = setOf(
        "gmail.com", "hotmail.com", "outlook.com", "yahoo.com",
        "icloud.com", "protonmail.com", "aol.com", "mail.com",
        "gmx.com", "yandex.com",
        "ucatolica.edu.co", "outlook.es", "hotmail.es", "gmail.es", "yahoo.es"
    )

    fun isValidEmail(email: String): Pair<Boolean, String> {
        if (email.isBlank()) return Pair(false, "Por favor ingrese su correo electrónico")
        if (email.contains(" ")) return Pair(false, "El correo no debe contener espacios")

        val atCount = email.count { it == '@' }
        if (atCount == 0) return Pair(false, "El correo debe contener '@'")
        if (atCount > 1) return Pair(false, "El correo no puede tener múltiples '@'")

        val parts = email.split('@')
        if (parts.size != 2) return Pair(false, "Formato de correo inválido")

        val localPart = parts[0]
        val domain = parts[1].lowercase()

        if (localPart.isEmpty()) return Pair(false, "El correo debe tener un usuario antes del '@'")
        if (domain.isEmpty()) return Pair(false, "El correo debe tener un dominio después del '@'")
        if (!domain.contains(".")) return Pair(false, "El dominio debe contener un punto (ej: gmail.com)")

        val commonTypos = mapOf(
            "gamil.com" to "gmail.com", "gmial.com" to "gmail.com",
            "hotmal.com" to "hotmail.com", "outlok.com" to "outlook.com",
            "yahoo.com" to "yahoo.com", "gmil.com" to "gmail.com",
            "hotmai.com" to "hotmail.com", "outloo.com" to "outlook.com"
        )

        for ((typo, correct) in commonTypos) {
            if (domain == typo) return Pair(false, "Dominio incorrecto. ¿Quiso decir @$correct?")
        }

        val isValidDomain = validDomains.any { domain == it || domain.endsWith(".$it") }
        if (!isValidDomain) {
            val suggestion = validDomains.find { domain.contains(it.split(".")[0]) }
            val message = if (suggestion != null) {
                "Dominio no válido. ¿Quiso decir @$suggestion?"
            } else {
                "Dominio no soportado. Usa: Gmail, Hotmail, Outlook, Yahoo"
            }
            return Pair(false, message)
        }

        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) return Pair(false, "Formato de correo electrónico inválido")

        return Pair(true, "")
    }

    LaunchedEffect(sendCodeState) {
        when (sendCodeState) {
            is SendCodeState.Success -> {
                val message = (sendCodeState as SendCodeState.Success).message
                scope.launch { snackbarHostState.showSnackbar("📧 $message") }
                currentStep = 2
                timeLeft = 60
                canResend = false
            }
            is SendCodeState.Error -> {
                emailError = (sendCodeState as SendCodeState.Error).message
                scope.launch { snackbarHostState.showSnackbar("❌ ${(sendCodeState as SendCodeState.Error).message}") }
            }
            else -> {}
        }
    }

    // Manejar respuestas de verificación de código
    LaunchedEffect(verifyCodeState) {
        when (verifyCodeState) {
            is VerifyCodeState.Success -> {
                val message = (verifyCodeState as VerifyCodeState.Success).message
                scope.launch {
                    snackbarHostState.showSnackbar("✅ $message")
                }
                currentStep = 3
            }
            is VerifyCodeState.Error -> {
                val error = (verifyCodeState as VerifyCodeState.Error).message
                codeError = error

                // Si el código expiró, regresar al paso 1
                if (error.contains("expiró") || error.contains("expirado") || error.contains("tiempo") || error.contains("400")) {
                    scope.launch {
                        snackbarHostState.showSnackbar("⚠ El código ha expirado. Por favor solicita uno nuevo.")
                    }
                    // Limpiar campos y volver al paso 1
                    currentStep = 1
                    inputCode = ""
                    codeError = null
                    timeLeft = 300  // Resetear timer por si acaso
                    canResend = false
                } else if (error.contains("inválido") == true || error.contains("incorrecto") == true) {
                    inputCode = ""
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("✅ ¡Registro exitoso! Por favor inicia sesión")
                }

                // Navegamos al Login después del registro
                navController.navigate("login") {
                    popUpTo("registro") { inclusive = true }
                }
            }
            is RegisterState.Error -> {
                val error = (registerState as RegisterState.Error).message
                scope.launch { snackbarHostState.showSnackbar("❌ Error: $error") }
            }
            else -> {}
        }
    }
// Timer 1: Para habilitar el botón de reenviar código (60 segundos)
    LaunchedEffect(currentStep, timeLeft) {
        if (currentStep == 2 && timeLeft > 0 && !canResend) {
            delay(1000)
            timeLeft--
            if (timeLeft == 0) canResend = true
        }
    }

// Timer 2: Para expiración total del código (5 minutos = 300 segundos)
    LaunchedEffect(currentStep) {
        if (currentStep == 2) {
            delay(300000) // 5 minutos en milisegundos
            // Si después de 5 minutos aún estamos en el paso 2, regresar al paso 1
            if (currentStep == 2) {
                scope.launch {
                    snackbarHostState.showSnackbar("⚠️ El tiempo para ingresar el código ha expirado. Por favor solicita uno nuevo.")
                }
                currentStep = 1
                inputCode = ""
                codeError = null
                timeLeft = 60 // Resetear timer de reenvío
                canResend = false
            }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(step = 1, currentStep = currentStep, text = "Email")
                        HorizontalDivider(modifier = Modifier.width(40.dp), thickness = 2.dp,
                            color = if (currentStep > 1) AquamarineDark else Color(0xFFE0E0E0))
                        StepIndicator(step = 2, currentStep = currentStep, text = "Código")
                        HorizontalDivider(modifier = Modifier.width(40.dp), thickness = 2.dp,
                            color = if (currentStep > 2) AquamarineDark else Color(0xFFE0E0E0))
                        StepIndicator(step = 3, currentStep = currentStep, text = "Registro")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    when (currentStep) {
                        1 -> Paso1Email(
                            email = email,
                            onEmailChange = { email = it; emailError = null },
                            emailError = emailError,
                            isSendingCode = sendCodeState is SendCodeState.Loading,
                            onSendCode = {
                                val (isValid, errorMessage) = isValidEmail(email)
                                if (!isValid) {
                                    emailError = errorMessage
                                    return@Paso1Email
                                }
                                authViewModel.sendCode(email)
                            }
                        )

                        2 -> Paso2VerificarCodigo(
                            inputCode = inputCode,
                            onInputCodeChange = { inputCode = it; codeError = null },
                            codeError = codeError,
                            isVerifying = verifyCodeState is VerifyCodeState.Loading,
                            timeLeft = timeLeft,
                            canResend = canResend,
                            onVerify = {
                                if (inputCode.isBlank()) {
                                    codeError = "Por favor ingrese el código de verificación"
                                    return@Paso2VerificarCodigo
                                }
                                if (inputCode.length != 6) {
                                    codeError = "El código debe tener exactamente 6 dígitos"
                                    return@Paso2VerificarCodigo
                                }
                                if (!inputCode.all { it.isDigit() }) {
                                    codeError = "El código debe contener solo números"
                                    return@Paso2VerificarCodigo
                                }
                                authViewModel.verifyCode(email, inputCode)
                            },
                            onResend = {
                                if (canResend) {
                                    authViewModel.sendCode(email)
                                    timeLeft = 60
                                    canResend = false
                                    inputCode = ""
                                    codeError = null
                                }
                            }
                        )

                        3 -> Paso3RegistroCompleto(
                            nombre = nombre,
                            onNombreChange = { nombre = it },
                            password = password,
                            onPasswordChange = { password = it },
                            confirmPassword = confirmPassword,
                            onConfirmPasswordChange = { confirmPassword = it },
                            isPasswordVisible = isPasswordVisible,
                            onPasswordVisibilityChange = { isPasswordVisible = it },
                            isConfirmPasswordVisible = isConfirmPasswordVisible,
                            onConfirmPasswordVisibilityChange = { isConfirmPasswordVisible = it },
                            isRegistering = registerState is RegisterState.Loading,
                            registerError = (registerState as? RegisterState.Error)?.message,
                            selectedImageUri = selectedImageUri,
                            onImageSelected = { selectedImageUri = it },
                            onRegister = {
                                when {
                                    nombre.isBlank() -> {
                                        scope.launch { snackbarHostState.showSnackbar("Por favor ingrese su nombre completo") }
                                        return@Paso3RegistroCompleto
                                    }
                                    password.isBlank() -> {
                                        scope.launch { snackbarHostState.showSnackbar("Por favor ingrese una contraseña") }
                                        return@Paso3RegistroCompleto
                                    }
                                    password.length < 8 -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña debe tener al menos 8 caracteres") }
                                        return@Paso3RegistroCompleto
                                    }
                                    !password.any { it.isUpperCase() } -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña debe contener al menos una letra mayúscula") }
                                        return@Paso3RegistroCompleto
                                    }
                                    !password.any { it.isLowerCase() } -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña debe contener al menos una letra minúscula") }
                                        return@Paso3RegistroCompleto
                                    }
                                    !password.any { it.isDigit() } -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña debe contener al menos un número") }
                                        return@Paso3RegistroCompleto
                                    }
                                    !password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?/~`" } -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña debe contener al menos un carácter especial (!@#$%^&*)") }
                                        return@Paso3RegistroCompleto
                                    }
                                    password.contains(" ") -> {
                                        scope.launch { snackbarHostState.showSnackbar("La contraseña no debe contener espacios") }
                                        return@Paso3RegistroCompleto
                                    }
                                    confirmPassword.isBlank() -> {
                                        scope.launch { snackbarHostState.showSnackbar("Por favor confirme su contraseña") }
                                        return@Paso3RegistroCompleto
                                    }
                                    password != confirmPassword -> {
                                        scope.launch { snackbarHostState.showSnackbar("Las contraseñas no coinciden") }
                                        return@Paso3RegistroCompleto
                                    }
                                }
                                authViewModel.register(context, nombre, email, password, inputCode, null, selectedImageUri)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "v1.0.0", fontSize = 12.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, currentStep: Int, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (currentStep >= step) AquamarineDark else Color(0xFFE0E0E0)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = step.toString(), color = if (currentStep >= step) Color.White else Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = text, fontSize = 10.sp, color = if (currentStep >= step) AquamarineDark else Color.Gray)
    }
}

@Composable
fun Paso1Email(
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    isSendingCode: Boolean,
    onSendCode: () -> Unit
) {
    Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(24.dp), color = AquamarineDark.copy(alpha = 0.12f)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = AquamarineDark, modifier = Modifier.size(44.dp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("VERIFICAR CORREO", style = MaterialTheme.typography.headlineSmall, color = AquamarineDark, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    Text("Ingresa tu correo para recibir el código de verificación", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666), textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(32.dp))

    if (isSendingCode) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AquamarinePrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Enviando código...", fontSize = 12.sp, color = Color(0xFF666666))
        Spacer(modifier = Modifier.height(8.dp))
    }

    emailError?.let {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(), border = BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.5f))) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = it, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Correo electrónico") },
        placeholder = { Text("usuario@gmail.com") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AquamarineDark) },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color(0xFFBDBDBD)),
        isError = emailError != null)

    Spacer(modifier = Modifier.height(8.dp))

    Card(colors = CardDefaults.cardColors(containerColor = AquamarineLight), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "Info", tint = AquamarineDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Dominios válidos:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Gmail, Hotmail, Outlook, Yahoo, iCloud", fontSize = 10.sp, color = AquamarineDark)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onSendCode, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !isSendingCode,
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary, contentColor = Color.White)) {
        if (isSendingCode) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        else Text("ENVIAR CÓDIGO", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
fun Paso2VerificarCodigo(
    inputCode: String,
    onInputCodeChange: (String) -> Unit,
    codeError: String?,
    isVerifying: Boolean,
    timeLeft: Int,
    canResend: Boolean,
    onVerify: () -> Unit,
    onResend: () -> Unit
) {
    Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(24.dp), color = AquamarineDark.copy(alpha = 0.12f)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Verified, contentDescription = "Código", tint = AquamarineDark, modifier = Modifier.size(44.dp))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("VERIFICAR CÓDIGO", style = MaterialTheme.typography.headlineSmall, color = AquamarineDark, fontWeight = FontWeight.Bold)
    Text("Ingresa el código que enviamos a tu correo", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666), textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(32.dp))

    if (isVerifying) {
        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AquamarinePrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Verificando código...", fontSize = 12.sp, color = Color(0xFF666666))
        Spacer(modifier = Modifier.height(8.dp))
    }

    codeError?.let {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = it, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
            }
        }
    }

    OutlinedTextField(value = inputCode, onValueChange = { if (it.length <= 6) onInputCodeChange(it) },
        label = { Text("Código de verificación") }, placeholder = { Text("000000") }, modifier = Modifier.fillMaxWidth(),
        singleLine = true, shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AquamarineDark) },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color(0xFFBDBDBD)),
        isError = codeError != null)

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(onClick = onResend, enabled = canResend) {
        Text(if (canResend) "Reenviar código" else "Reenviar código en ${timeLeft}s", color = if (canResend) AquamarineDark else Color.Gray)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = onVerify, modifier = Modifier.fillMaxWidth().height(56.dp), enabled = !isVerifying,
        shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary, contentColor = Color.White)) {
        if (isVerifying) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        else Text("VERIFICAR", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Paso3RegistroCompleto(
    nombre: String,
    onNombreChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    isConfirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit,
    isRegistering: Boolean,
    registerError: String?,
    selectedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onRegister: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }

    // Envolver todo en un Column con verticalScroll
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector de imagen de perfil al inicio
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { launcher.launch("image/*") }
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Agregar foto",
                            tint = AquamarineDark,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Agregar foto",
                            fontSize = 11.sp,
                            color = AquamarineDark
                        )
                    }
                }
            }
            Text(
                text = if (selectedImageUri != null) "Cambiar foto" else "Toca para agregar foto",
                fontSize = 11.sp,
                color = AquamarineDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("COMPLETAR REGISTRO", style = MaterialTheme.typography.headlineSmall, color = AquamarineDark, fontWeight = FontWeight.Bold)
        Text("Completa tus datos para finalizar el registro", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666), textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(32.dp))

        if (isRegistering) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), color = AquamarinePrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Creando cuenta...", fontSize = 12.sp, color = Color(0xFF666666))
            Spacer(modifier = Modifier.height(8.dp))
        }

        registerError?.let {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = "Error", tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = it, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        OutlinedTextField(value = nombre, onValueChange = onNombreChange, label = { Text("Nombre completo") },
            placeholder = { Text("Juan Pérez") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(16.dp), leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AquamarineDark) },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color(0xFFBDBDBD)))

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = password, onValueChange = onPasswordChange, label = { Text("Contraseña") },
            placeholder = { Text("Ej: Password123#") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(16.dp), visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AquamarineDark) },
            trailingIcon = {
                IconButton(onClick = { onPasswordVisibilityChange(!isPasswordVisible) }) {
                    Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = AquamarineDark)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color(0xFFBDBDBD)))

        Spacer(modifier = Modifier.height(8.dp))

        // Requisitos de contraseña
        Card(colors = CardDefaults.cardColors(containerColor = AquamarineLight), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "🔒 Requisitos de contraseña:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AquamarineDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "• Mínimo 8 caracteres\n• Una letra MAYÚSCULA\n• Una letra minúscula\n• Un número\n• Un carácter especial (!@#$%^&*)",
                    fontSize = 10.sp, color = AquamarineDark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = confirmPassword, onValueChange = onConfirmPasswordChange, label = { Text("Confirmar contraseña") },
            placeholder = { Text("Repite tu contraseña") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(16.dp), visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AquamarineDark) },
            trailingIcon = {
                IconButton(onClick = { onConfirmPasswordVisibilityChange(!isConfirmPasswordVisible) }) {
                    Icon(if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = AquamarineDark)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AquamarinePrimary, unfocusedBorderColor = Color(0xFFBDBDBD)))

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !isRegistering,
            shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = AquamarinePrimary, contentColor = Color.White)) {
            if (isRegistering) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("REGISTRARSE", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(modifier = Modifier.height(32.dp)) // Espacio extra al final
    }
}