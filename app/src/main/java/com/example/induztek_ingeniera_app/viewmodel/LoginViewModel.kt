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

// =====================================================================
//  LoginViewModel.kt  —  LÓGICA DE LA PANTALLA DE INICIO DE SESIÓN (Room & MVVM)
//  ---------------------------------------------------------------------
//  ViewModel estándar: Recibe el repositorio inyectado desde la Factory
//  en MainActivity. Las consultas se ejecutan de forma asíncrona.
// =====================================================================

class LoginViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // Privado y mutable: solo el ViewModel lo modifica
    private val _uiState = MutableStateFlow(LoginUiState())

    // Público y de solo lectura: la pantalla lo observa
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoCambia(valor: String) {
        _uiState.update {
            it.copy(correo = valor.trim(), errorCorreo = null, errorCredenciales = null, usuarioIngresado = null)
        }
    }

    fun onContrasenaCambia(valor: String) {
        _uiState.update {
            it.copy(contrasena = valor, errorContrasena = null, errorCredenciales = null, usuarioIngresado = null)
        }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    // Botón "Ingresar": Valida campos y realiza la consulta asíncrona en Room
    fun onIngresar() {
        val estado = _uiState.value

        // 1) Validación por campo en la UI
        val errorCorreo = when {
            estado.correo.isBlank() -> "Ingresa tu correo"
            !estado.correo.contains("@") || !estado.correo.contains(".") -> "Ingresa un correo válido"
            else -> null
        }
        val errorContrasena = when {
            estado.contrasena.isBlank() -> "Ingresa tu contraseña"
            estado.contrasena.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        if (errorCorreo != null || errorContrasena != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena) }
            return
        }

        // 2) Lanzamiento de Corrutina asíncrona para consultar la base de datos de Room sin congelar la pantalla
        viewModelScope.launch {
            val usuarioRoom = repository.iniciarSesion(estado.correo, estado.contrasena)

            if (usuarioRoom != null) {
                // Mapeo opcional al modelo de dominio Usuario si es requerido por tus pantallas,
                // de lo contrario, se guarda directamente la entidad.
                _uiState.update {
                    it.copy(
                        errorCredenciales = null,
                        // Aquí puedes pasar el objeto mapeado si tu UIState maneja la entidad o el modelo
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

    // Nota: El método onCleared() ya no necesita cerrar la conexión explícitamente ya que
    // AppDatabase (Room) maneja el pool de conexiones de manera automática y eficiente.
}
