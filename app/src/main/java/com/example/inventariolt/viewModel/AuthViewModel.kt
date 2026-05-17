package com.example.inventariolt.viewModel


import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventariolt.model.login.LoginResponseDTO
import com.example.inventariolt.model.login.UsuarioResponseDTO
import com.example.inventariolt.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _sendCodeState = MutableStateFlow<SendCodeState>(SendCodeState.Idle)
    val sendCodeState: StateFlow<SendCodeState> = _sendCodeState

    private val _verifyCodeState = MutableStateFlow<VerifyCodeState>(VerifyCodeState.Idle)
    val verifyCodeState: StateFlow<VerifyCodeState> = _verifyCodeState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun sendCode(correo: String) {
        viewModelScope.launch {
            _sendCodeState.value = SendCodeState.Loading
            val result = repository.sendCode(correo)
            result.onSuccess { message ->
                _sendCodeState.value = SendCodeState.Success(message)
            }.onFailure { error ->
                _sendCodeState.value =
                    SendCodeState.Error(error.message ?: "Error al enviar código")
            }
        }
    }

    fun verifyCode(correo: String, codigo: String) {
        viewModelScope.launch {
            _verifyCodeState.value = VerifyCodeState.Loading
            val result = repository.verifyCode(correo, codigo)
            result.onSuccess { message ->
                _verifyCodeState.value = VerifyCodeState.Success(message)
            }.onFailure { error ->
                _verifyCodeState.value = VerifyCodeState.Error(error.message ?: "Código incorrecto")
            }
        }
    }

    fun register(context: Context, nombre: String, correo: String, password: String, codigo: String, tiendaId: Long?, imageUri: Uri?) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = repository.register(context, nombre, correo, password, codigo, tiendaId, imageUri)
            result.onSuccess { user ->
                _registerState.value = RegisterState.Success(user)
            }.onFailure { error ->
                _registerState.value = RegisterState.Error(error.message ?: "Error al registrar")
            }
        }
    }

    fun login(correo: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            val result = repository.login(correo, password)
            result.onSuccess { user ->
                _loginState.value = LoginState.Success(user)
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun resetStates() {
        _sendCodeState.value = SendCodeState.Idle
        _verifyCodeState.value = VerifyCodeState.Idle
        _registerState.value = RegisterState.Idle
        _loginState.value = LoginState.Idle
    }

    fun sendCodeWithOkHttp(correo: String) {
        viewModelScope.launch {
            _sendCodeState.value = SendCodeState.Loading
            val result = repository.sendCodeWithOkHttp(correo)
            result.onSuccess { message ->
                _sendCodeState.value = SendCodeState.Success(message)
            }.onFailure { error ->
                _sendCodeState.value =
                    SendCodeState.Error(error.message ?: "Error al enviar código")
            }
        }
    }
}
sealed class SendCodeState {
    object Idle : SendCodeState()
    object Loading : SendCodeState()
    data class Success(val message: String) : SendCodeState()
    data class Error(val message: String) : SendCodeState()
}

sealed class VerifyCodeState {
    object Idle : VerifyCodeState()
    object Loading : VerifyCodeState()
    data class Success(val message: String) : VerifyCodeState()
    data class Error(val message: String) : VerifyCodeState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: UsuarioResponseDTO) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoginResponseDTO) : LoginState()
    data class Error(val message: String) : LoginState()
}