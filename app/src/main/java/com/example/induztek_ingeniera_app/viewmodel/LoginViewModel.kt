package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.data.hashRespuestaSeguridad
import com.example.induztek_ingeniera_app.model.LoginUiState
import com.example.induztek_ingeniera_app.model.Rol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroClienteUiState(
    val nombre: String = "",
    val apellido: String = "",
    val rut: String = "",
    val telefono: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val preguntaSeguridad: String = "",
    val respuestaSeguridad: String = "",
    val contrasenaVisible: Boolean = false,
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorRut: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmar: String? = null,
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null,
    val errorGeneral: String? = null,
    val registroExitoso: Boolean = false
)

class LoginViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _registroState = MutableStateFlow(RegistroClienteUiState())
    val registroState: StateFlow<RegistroClienteUiState> = _registroState.asStateFlow()

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
                    it.copy(errorCredenciales = null, usuarioIngresado = usuarioRoom)
                }
            } else {
                _uiState.update {
                    it.copy(errorCredenciales = "Correo o contraseña incorrectos")
                }
            }
        }
    }

    // --- FLUJO DE REGISTRO CLIENTE ---
    fun limpiarRegistro() {
        _registroState.value = RegistroClienteUiState()
    }

    fun onRegNombreCambia(v: String) = _registroState.update { it.copy(nombre = v, errorNombre = null, errorGeneral = null) }
    fun onRegApellidoCambia(v: String) = _registroState.update { it.copy(apellido = v, errorApellido = null, errorGeneral = null) }
    fun onRegRutCambia(v: String) = _registroState.update { it.copy(rut = v, errorRut = null, errorGeneral = null) }
    fun onRegTelefonoCambia(v: String) = _registroState.update { it.copy(telefono = v, errorGeneral = null) }
    fun onRegCorreoCambia(v: String) = _registroState.update { it.copy(correo = v, errorCorreo = null, errorGeneral = null) }
    fun onRegContrasenaCambia(v: String) = _registroState.update { it.copy(contrasena = v, errorContrasena = null, errorGeneral = null) }
    fun onRegConfirmarCambia(v: String) = _registroState.update { it.copy(confirmarContrasena = v, errorConfirmar = null, errorGeneral = null) }
    fun onRegPreguntaCambia(v: String) = _registroState.update { it.copy(preguntaSeguridad = v, errorPregunta = null, errorGeneral = null) }
    fun onRegRespuestaCambia(v: String) = _registroState.update { it.copy(respuestaSeguridad = v, errorRespuesta = null, errorGeneral = null) }
    fun onRegAlternarVisibilidad() = _registroState.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }

    fun registrarCliente() {
        val s = _registroState.value
        val nombreLimpio = s.nombre.trim()
        val apellidoLimpio = s.apellido.trim()
        val rutLimpio = s.rut.trim()
        val correoLimpio = s.correo.trim().lowercase()
        val passLimpia = s.contrasena.trim()
        val confirmarLimpia = s.confirmarContrasena.trim()
        val preguntaLimpia = s.preguntaSeguridad.trim()
        val respuestaLimpia = s.respuestaSeguridad.trim()

        val errNombre = if (nombreLimpio.isBlank()) "Ingresa tu nombre" else null
        val errApellido = if (apellidoLimpio.isBlank()) "Ingresa tu apellido" else null
        val errRut = if (rutLimpio.isBlank()) "Ingresa tu RUT" else null
        val errCorreo = when {
            correoLimpio.isBlank() -> "Ingresa tu correo"
            !correoLimpio.contains("@") || !correoLimpio.contains(".") -> "Correo inválido"
            else -> null
        }
        val errPass = when {
            passLimpia.isBlank() -> "Ingresa una contraseña"
            passLimpia.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        val errConfirmar = if (confirmarLimpia != passLimpia) "Las contraseñas no coinciden" else null
        val errPregunta = if (preguntaLimpia.isBlank()) "Ingresa una pregunta de seguridad" else null
        val errRespuesta = if (respuestaLimpia.length < 2) "Mínimo 2 caracteres" else null

        if (listOf(errNombre, errApellido, errRut, errCorreo, errPass, errConfirmar, errPregunta, errRespuesta).any { it != null }) {
            _registroState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorApellido = errApellido,
                    errorRut = errRut,
                    errorCorreo = errCorreo,
                    errorContrasena = errPass,
                    errorConfirmar = errConfirmar,
                    errorPregunta = errPregunta,
                    errorRespuesta = errRespuesta
                )
            }
            return
        }

        viewModelScope.launch {
            if (repository.existeCorreo(correoLimpio)) {
                _registroState.update { it.copy(errorCorreo = "Este correo ya está registrado") }
                return@launch
            }
            if (repository.existeRut(rutLimpio)) {
                _registroState.update { it.copy(errorRut = "Este RUT ya está registrado") }
                return@launch
            }

            val nuevoCliente = UsuarioEntity(
                nombre = nombreLimpio,
                apellido = apellidoLimpio,
                rut = rutLimpio,
                telefono = s.telefono.trim(),
                correo = correoLimpio,
                contrasena = passLimpia,
                idRol = Rol.ID_CLIENTE,
                idEmpresa = null,
                preguntaSeguridad = preguntaLimpia,
                respuestaSeguridad = hashRespuestaSeguridad(respuestaLimpia)
            )

            val idCreado = repository.crearUsuario(nuevoCliente)
            if (idCreado > 0) {
                // Pre-rellena el correo en la pantalla de login para comodidad del cliente
                _uiState.update { it.copy(correo = correoLimpio, contrasena = "", errorCredenciales = null) }
                _registroState.update { it.copy(registroExitoso = true) }
            } else {
                _registroState.update { it.copy(errorGeneral = "No se pudo completar el registro. Verifica tus datos.") }
            }
        }
    }

    fun cerrarSesion() {
        _uiState.value = LoginUiState()
    }
}