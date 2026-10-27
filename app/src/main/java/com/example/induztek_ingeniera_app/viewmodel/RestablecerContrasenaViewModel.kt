package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.model.RestablecerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
//  RestablecerContrasenaViewModel.kt  —  LÓGICA DE "RESTABLECER CONTRASEÑA"
//  ---------------------------------------------------------------------
//  Junta dos cosas que ya conoces:
//   - FormularioViewModel del profe: valida cada campo con "when" y marca el éxito.
//   - LoginViewModel: si los campos están bien, va a la BD (Room) dentro de una corrutina.
//
//  Recibe el Repository por el constructor, por eso la Factory de MainActivity
//  es la que lo crea (viewModel() solo sabe crear ViewModels sin parámetros).
//
//  OJO (app real): una app de verdad primero manda un código al correo y solo
//  después deja cambiar la clave. Aquí todo es local (no hay servidor que mande
//  correos), así que basta con que el correo exista en la BD.
// =====================================================================

class RestablecerContrasenaViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // Privado y mutable: solo el ViewModel lo modifica
    private val _uiState = MutableStateFlow(RestablecerUiState())

    // Público y de solo lectura: la pantalla lo observa
    val uiState: StateFlow<RestablecerUiState> = _uiState.asStateFlow()

    // Cada letra escrita llega aquí. Se borra el error de ESE campo y el mensaje
    // de éxito, porque el usuario está corrigiendo (como registroExitoso = false
    // en el FormularioViewModel del profe).
    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, cambioExitoso = false) }
    }

    fun onNuevaCambia(valor: String) {
        _uiState.update { it.copy(nueva = valor, errorNueva = null, cambioExitoso = false) }
    }

    fun onConfirmarCambia(valor: String) {
        _uiState.update { it.copy(confirmar = valor, errorConfirmar = null, cambioExitoso = false) }
    }

    // El "ojo": muestra u oculta las dos contraseñas a la vez
    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    // Botón "Cambiar contraseña": 1) valida cada campo y 2) si están bien, hace el UPDATE.
    fun onCambiar() {
        val estado = _uiState.value

        // Se limpian IGUAL que en LoginViewModel (trim = quitar espacios de los bordes).
        // Si aquí se guardara " clave1" con un espacio, el login (que sí lo quita)
        // nunca coincidiría con lo guardado y el usuario no podría entrar.
        val correo = estado.correo.trim().lowercase()
        val nueva = estado.nueva.trim()
        val confirmar = estado.confirmar.trim()

        // 1) Validación por campo: cada campo tiene su propio mensaje de error
        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !correo.contains("@") || !correo.contains(".") -> "Ingresa un correo válido"
            else -> null
        }
        val errorNueva = when {
            nueva.isBlank() -> "Ingresa la nueva contraseña"
            nueva.length < 6 -> "Mínimo 6 caracteres"          // la misma regla del login
            else -> null
        }
        val errorConfirmar = when {
            confirmar.isBlank() -> "Repite la nueva contraseña"
            confirmar != nueva -> "Las contraseñas no coinciden"
            else -> null
        }

        // Si algún campo tiene error, se muestran TODOS los errores y no se toca la BD
        if (errorCorreo != null || errorNueva != null || errorConfirmar != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorNueva = errorNueva, errorConfirmar = errorConfirmar)
            }
            return
        }

        // 2) Los campos están bien: UPDATE en Room. Como es una función "suspend",
        //    va dentro de viewModelScope.launch (una corrutina que se cancela sola
        //    si el ViewModel se destruye).
        viewModelScope.launch {
            val cambio = repository.cambiarContrasena(correo, nueva)
            _uiState.update {
                if (cambio) it.copy(cambioExitoso = true)                          // cambió 1 fila
                else it.copy(errorCorreo = "No existe una cuenta con ese correo")   // cambió 0 filas
            }
        }
    }

    // Vuelve al estado inicial (como onLimpiar() del profe).
    // Se llama al ENTRAR a la pantalla: este ViewModel vive mientras la app está
    // abierta (se crea en MainActivity) y, sin limpiar, mostraría lo escrito la vez anterior.
    fun limpiar() {
        _uiState.value = RestablecerUiState()
    }
}