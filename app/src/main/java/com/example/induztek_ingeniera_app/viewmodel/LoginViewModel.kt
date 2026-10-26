package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.model.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(correo = valor, errorCorreo = null, errorCredenciales = null)
        }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(contrasena = valor, errorContrasena = null, errorCredenciales = null)
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onIngresar() {
        val estado = _uiState.value

        // Limpiamos espacios invisibles y convertimos a minúsculas antes de consultar
        val correoLimpio = estado.correo.trim().lowercase()
        val contrasenaLimpia = estado.contrasena.trim()

        val errorCorreo = when {
            correoLimpio.isBlank() -> "Ingresa tu correo"
            !correoLimpio.contains("@") || !correoLimpio.contains(".") -> "Ingresa un correo válido"
            else -> null
        }
        val errorContrasena = when {
            contrasenaLimpia.isBlank() -> "Ingresa tu contraseña"
            contrasenaLimpia.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return
        }

        viewModelScope.launch {
            val usuarioRoom = repository.iniciarSesion(correoLimpio, contrasenaLimpia)

            if (usuarioRoom != null) {
                _uiState.update {
                    it.copy(
                        errorCredenciales = null,
                        usuarioIngresado = com.example.induztek_ingeniera_app.model.Usuario(
                            id = usuarioRoom.idUsuario,
                            nombre = usuarioRoom.nombre,
                            correo = usuarioRoom.correo,
                            empresa = usuarioRoom.empresa,
                            rolId = usuarioRoom.idRol,
                            rolNombre = if (usuarioRoom.idRol == 1L) "Técnico" else "Cliente"
                        )
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorCredenciales = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}