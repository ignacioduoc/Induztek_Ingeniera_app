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


class RestablecerContrasenaViewModel(private val repository: ProtocoloRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(RestablecerUiState())


    val uiState: StateFlow<RestablecerUiState> = _uiState.asStateFlow()


    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, cambioExitoso = false) }
    }

    fun onNuevaCambia(valor: String) {
        _uiState.update { it.copy(nueva = valor, errorNueva = null, cambioExitoso = false) }
    }

    fun onConfirmarCambia(valor: String) {
        _uiState.update { it.copy(confirmar = valor, errorConfirmar = null, cambioExitoso = false) }
    }

    fun onAlternarVisibilidad() {
        _uiState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }
    }

    fun onCambiar() {
        val estado = _uiState.value


        val correo = estado.correo.trim().lowercase()
        val nueva = estado.nueva.trim()
        val confirmar = estado.confirmar.trim()


        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !correo.contains("@") || !correo.contains(".") -> "Ingresa un correo válido"
            else -> null
        }
        val errorNueva = when {
            nueva.isBlank() -> "Ingresa la nueva contraseña"
            nueva.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        val errorConfirmar = when {
            confirmar.isBlank() -> "Repite la nueva contraseña"
            confirmar != nueva -> "Las contraseñas no coinciden"
            else -> null
        }


        if (errorCorreo != null || errorNueva != null || errorConfirmar != null) {
            _uiState.update {
                it.copy(errorCorreo = errorCorreo, errorNueva = errorNueva, errorConfirmar = errorConfirmar)
            }
            return
        }


        viewModelScope.launch {
            val cambio = repository.cambiarContrasena(correo, nueva)
            _uiState.update {
                if (cambio) it.copy(cambioExitoso = true)                          // cambió 1 fila
                else it.copy(errorCorreo = "No existe una cuenta con ese correo")   // cambió 0 filas
            }
        }
    }


    fun limpiar() {
        _uiState.value = RestablecerUiState()
    }
}