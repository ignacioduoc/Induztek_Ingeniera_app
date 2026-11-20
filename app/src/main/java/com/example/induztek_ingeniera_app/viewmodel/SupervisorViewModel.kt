package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevoTecnicoUiState(
    val nombre: String = "",
    val apellido: String = "",
    val rut: String = "",
    val telefono: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorRut: String? = null,
    val errorTelefono: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val mensajeExito: String? = null
)

class SupervisorViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    val equipos: StateFlow<List<EquipoConDetalles>> = repository.obtenerTodosLosEquipos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tecnicos: StateFlow<List<UsuarioEntity>> = repository.listarUsuarios()
        .map { lista -> lista.filter { it.idRol == Rol.ID_TECNICO } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _nuevoTecnicoState = MutableStateFlow(NuevoTecnicoUiState())
    val nuevoTecnicoState: StateFlow<NuevoTecnicoUiState> = _nuevoTecnicoState.asStateFlow()

    fun asignarTecnico(idEquipo: Long, idTecnico: Long?) {
        viewModelScope.launch {
            repository.asignarTecnicoAEquipo(idEquipo, idTecnico)
        }
    }

    fun limpiarFormularioTecnico() {
        _nuevoTecnicoState.value = NuevoTecnicoUiState()
    }

    fun onNombreCambia(v: String) = _nuevoTecnicoState.update { it.copy(nombre = v, errorNombre = null, mensajeExito = null) }
    fun onApellidoCambia(v: String) = _nuevoTecnicoState.update { it.copy(apellido = v, errorApellido = null, mensajeExito = null) }
    fun onRutCambia(v: String) = _nuevoTecnicoState.update { it.copy(rut = v, errorRut = null, mensajeExito = null) }
    fun onTelefonoCambia(v: String) = _nuevoTecnicoState.update { it.copy(telefono = v, errorTelefono = null, mensajeExito = null) }
    fun onCorreoCambia(v: String) = _nuevoTecnicoState.update { it.copy(correo = v, errorCorreo = null, mensajeExito = null) }
    fun onContrasenaCambia(v: String) = _nuevoTecnicoState.update { it.copy(contrasena = v, errorContrasena = null, mensajeExito = null) }

    fun crearCuentaTecnico(onCreado: () -> Unit = {}) {
        val s = _nuevoTecnicoState.value
        val correo = s.correo.trim().lowercase()
        val rutNormalizado = Validaciones.normalizarRut(s.rut)

        val errNombre = if (!Validaciones.nombreValido(s.nombre)) "Usa solo letras (mínimo 2)" else null
        val errApellido = if (!Validaciones.nombreValido(s.apellido)) "Usa solo letras (mínimo 2)" else null
        val errRut = when {
            s.rut.isBlank() -> "Ingresa el RUT"
            rutNormalizado == null -> "RUT inválido (revisa el dígito verificador)"
            else -> null
        }
        val errTelefono = if (!Validaciones.telefonoValido(s.telefono)) "Ingresa 9 dígitos, parte con 9 (Ej: 912345678)" else null
        val errCorreo = when {
            correo.isBlank() -> "Ingresa el correo"
            !Validaciones.correoValido(correo) -> "Ingresa un correo válido"
            else -> null
        }
        val errPass = when {
            s.contrasena.isBlank() -> "Ingresa una contraseña temporal"
            s.contrasena.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        if (listOf(errNombre, errApellido, errRut, errTelefono, errCorreo, errPass).any { it != null }) {
            _nuevoTecnicoState.update {
                it.copy(
                    errorNombre = errNombre,
                    errorApellido = errApellido,
                    errorRut = errRut,
                    errorTelefono = errTelefono,
                    errorCorreo = errCorreo,
                    errorContrasena = errPass
                )
            }
            return
        }

        viewModelScope.launch {
            if (repository.existeCorreo(correo)) {
                _nuevoTecnicoState.update { it.copy(errorCorreo = "Ya existe un usuario con ese correo") }
                return@launch
            }
            if (repository.existeRut(rutNormalizado!!)) {
                _nuevoTecnicoState.update { it.copy(errorRut = "Ya existe un usuario con ese RUT") }
                return@launch
            }

            // preguntaSeguridad vacía -> en el primer ingreso del técnico la app le pedirá configurarla
            val nuevoTecnico = UsuarioEntity(
                nombre = s.nombre.trim(),
                apellido = s.apellido.trim(),
                rut = rutNormalizado,
                telefono = "+56" + s.telefono.trim(),
                correo = correo,
                contrasena = s.contrasena,
                idRol = Rol.ID_TECNICO,
                idEmpresa = 1L
            )

            val id = repository.crearUsuario(nuevoTecnico)
            if (id > 0) {
                _nuevoTecnicoState.value = NuevoTecnicoUiState(mensajeExito = "Técnico creado correctamente")
                onCreado()
            }
        }
    }
}