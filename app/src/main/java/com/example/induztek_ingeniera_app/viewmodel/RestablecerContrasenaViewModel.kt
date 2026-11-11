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

    private companion object {
        const val MAX_INTENTOS = 3      // respuestas incorrectas permitidas antes de volver a empezar
    }

    private val _uiState = MutableStateFlow(RestablecerUiState())


    val uiState: StateFlow<RestablecerUiState> = _uiState.asStateFlow()


    fun onCorreoCambia(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null, cambioExitoso = false) }
    }

    fun onRespuestaCambia(valor: String) {
        _uiState.update { it.copy(respuesta = valor, errorRespuesta = null) }
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

    // Paso 1: comprueba el correo y busca la pregunta de seguridad de esa cuenta
    fun onContinuar() {
        val correo = _uiState.value.correo.trim().lowercase()

        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo"
            !correo.contains("@") || !correo.contains(".") -> "Ingresa un correo válido"
            else -> null
        }
        if (errorCorreo != null) {
            _uiState.update { it.copy(errorCorreo = errorCorreo) }
            return
        }

        viewModelScope.launch {
            val pregunta = repository.obtenerPreguntaSeguridad(correo)
            _uiState.update {
                when {
                    pregunta != null ->
                        it.copy(correo = correo, pregunta = pregunta, paso = 2, respuesta = "", intentos = 0)
                    repository.existeCorreo(correo) ->
                        // La cuenta existe pero aún no configuró su pregunta (cuenta nueva o reiniciada)
                        it.copy(errorCorreo = "Tu cuenta aún no tiene pregunta de seguridad. Contacta al administrador")
                    else ->
                        it.copy(errorCorreo = "No existe una cuenta con ese correo")
                }
            }
        }
    }

    // Paso 2: comprueba la respuesta. Si es correcta, pasa a elegir la contraseña nueva
    fun onVerificarRespuesta() {
        val estado = _uiState.value
        if (estado.respuesta.trim().length < 2) {
            _uiState.update { it.copy(errorRespuesta = "Escribe tu respuesta") }
            return
        }

        viewModelScope.launch {
            val correcta = repository.verificarRespuestaSeguridad(estado.correo, estado.respuesta)
            _uiState.update {
                when {
                    correcta -> it.copy(paso = 3, intentos = 0, errorRespuesta = null)
                    it.intentos + 1 >= MAX_INTENTOS ->
                        // Demasiados intentos: se vuelve al inicio y se borra lo escrito
                        RestablecerUiState(errorCorreo = "Demasiados intentos fallidos. Vuelve a comenzar")
                    else -> it.copy(
                        intentos = it.intentos + 1,
                        errorRespuesta = "Respuesta incorrecta (te quedan ${MAX_INTENTOS - it.intentos - 1} intentos)"
                    )
                }
            }
        }
    }

    // Vuelve al paso 1 para escribir otro correo
    fun onVolverAlCorreo() {
        _uiState.value = RestablecerUiState(correo = _uiState.value.correo)
    }

    // Paso 3: guarda la contraseña nueva (solo si la respuesta ya fue verificada)
    fun onCambiar() {
        val estado = _uiState.value
        if (estado.paso != 3) return

        val nueva = estado.nueva.trim()
        val confirmar = estado.confirmar.trim()

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

        if (errorNueva != null || errorConfirmar != null) {
            _uiState.update { it.copy(errorNueva = errorNueva, errorConfirmar = errorConfirmar) }
            return
        }

        viewModelScope.launch {
            // Se vuelve a comprobar la respuesta por seguridad antes de tocar la contraseña
            val autorizado = repository.verificarRespuestaSeguridad(estado.correo, estado.respuesta)

            // La contraseña nueva no puede ser igual a la que ya tiene la cuenta
            if (autorizado && repository.esContrasenaActual(estado.correo, nueva)) {
                _uiState.update { it.copy(errorNueva = "Debe ser distinta de tu contraseña actual") }
                return@launch
            }

            val cambio = autorizado && repository.cambiarContrasena(estado.correo, nueva)
            _uiState.update {
                if (cambio) it.copy(cambioExitoso = true, respuesta = "")
                else RestablecerUiState(errorCorreo = "No se pudo cambiar la contraseña. Vuelve a comenzar")
            }
        }
    }


    fun limpiar() {
        _uiState.value = RestablecerUiState()
    }
}