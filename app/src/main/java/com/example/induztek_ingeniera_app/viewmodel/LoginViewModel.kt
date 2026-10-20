package com.example.induztek_ingeniera_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.model.LoginUiState
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// =====================================================================
//  LoginViewModel.kt  —  LÓGICA DE LA PANTALLA DE INICIO DE SESIÓN
//  ---------------------------------------------------------------------
//  ViewModel -> AndroidViewModel: el Repository necesita un Context para
//  abrir la BD y la sesión. AndroidViewModel recibe la Application;
//  viewModel() lo sigue creando igual: no hace falta un Factory.
// =====================================================================

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    // Única puerta a los datos (BD + sesión)
    private val repositorio = ProtocoloRepository(application)

    // Privado y mutable: solo el ViewModel lo modifica
    private val _uiState = MutableStateFlow(LoginUiState())

    // Público y de solo lectura: la pantalla lo observa
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Cada letra escrita llega aquí. Se borran los errores y el resultado anterior,
    // porque el usuario está corrigiendo (como registroExitoso = false en el
    // FormularioViewModel del profe).
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

    // Botón "Ingresar": 1) valida cada campo y 2) si están bien, pregunta a la BD.
    // Devuelve el usuario si entró, o null si hubo errores.
    fun onIngresar(): Usuario? {
        val estado = _uiState.value

        // 1) Validación por campo: cada campo tiene su propio mensaje de error
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
            return null
        }

        // 2) Los campos están bien escritos: se busca al usuario en la BD
        //    (a través del Repository, que además guarda la sesión).
        val usuario = repositorio.iniciarSesion(estado.correo, estado.contrasena)

        _uiState.update {
            it.copy(
                errorCredenciales = if (usuario == null) "Correo o contraseña incorrectos" else null,
                usuarioIngresado = usuario
            )
        }
        return usuario
    }

    // Al destruirse el ViewModel se cierra la conexión con la BD
    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}