package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.FormularioUsuarioState
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.SecureRandom


class AdminUsuariosViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    val usuarios: StateFlow<List<Usuario>> = combine(
        repository.listarUsuarios(),
        repository.obtenerEmpresas()
    ) { entidades, empresas ->
        val empresaPorId = empresas.associateBy { it.idEmpresa }
        entidades.map { u ->
            val empresa = u.idEmpresa?.let { empresaPorId[it] }
            Usuario(
                id = u.idUsuario,
                nombre = u.nombre,
                apellido = u.apellido,
                correo = u.correo,
                rut = u.rut ?: "",
                telefono = u.telefono,
                empresa = empresa?.nombre ?: "",
                idEmpresa = u.idEmpresa ?: 0L,
                direccionEmpresa = empresa?.direccion ?: "",
                rolId = u.idRol,
                rolNombre = nombreDeRol(u.idRol),
                fotoPerfil = u.fotoPerfil
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val empresas: StateFlow<List<EmpresaEntity>> = repository.obtenerEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun nombreDeRol(idRol: Long): String = when (idRol) {
        Rol.ID_TECNICO -> "Técnico"
        Rol.ID_CLIENTE -> "Cliente"
        Rol.ID_ADMINISTRADOR -> "Administrador"
        Rol.ID_SUPERVISOR -> "Supervisor"
        else -> "Usuario"
    }

    private val _formulario = MutableStateFlow(FormularioUsuarioState())
    val formulario: StateFlow<FormularioUsuarioState> = _formulario.asStateFlow()

    // Almacena temporalmente la clave generada al azar solo para el Intent de correo y luego se limpia
    private var claveGeneradaPendiente: String? = null
    private var solicitarReinicioClave = false

    fun consumirClaveGeneradaParaCorreo(): String? {
        val temp = claveGeneradaPendiente
        claveGeneradaPendiente = null
        return temp
    }

    fun abrirFormulario(idUsuario: Long) {
        claveGeneradaPendiente = null
        solicitarReinicioClave = false
        if (idUsuario == 0L) {
            _formulario.value = FormularioUsuarioState()
            return
        }
        _formulario.value = FormularioUsuarioState(idUsuario = idUsuario, cargando = true)
        viewModelScope.launch {
            val u = repository.buscarUsuarioEntity(idUsuario)
            if (u != null) {
                _formulario.value = FormularioUsuarioState(
                    idUsuario = u.idUsuario,
                    nombre = u.nombre,
                    apellido = u.apellido,
                    rut = u.rut ?: "",
                    telefono = u.telefono.removePrefix("+56").filter { it.isDigit() },
                    correo = u.correo,
                    idRol = u.idRol,
                    idEmpresa = u.idEmpresa ?: 0L
                )
            } else {
                _formulario.update { it.copy(cargando = false, errorGeneral = "No se encontró el usuario") }
            }
        }
    }

    fun onNombreCambia(v: String) = _formulario.update { it.copy(nombre = v, errorNombre = null) }
    fun onApellidoCambia(v: String) = _formulario.update { it.copy(apellido = v, errorApellido = null) }
    fun onRutCambia(v: String) = _formulario.update { it.copy(rut = v, errorRut = null) }
    fun onTelefonoCambia(v: String) = _formulario.update { it.copy(telefono = v, errorTelefono = null) }
    fun onCorreoCambia(v: String) = _formulario.update { it.copy(correo = v, errorCorreo = null) }
    fun onRolCambia(v: Long) = _formulario.update { it.copy(idRol = v) }
    fun onEmpresaCambia(v: Long) = _formulario.update { it.copy(idEmpresa = v, errorEmpresa = null) }
    fun onContrasenaCambia(v: String) = _formulario.update { it.copy(contrasena = v, errorContrasena = null) }
    fun onAlternarVisibilidad() = _formulario.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }

    fun onReiniciarClaveAleatoria() {
        solicitarReinicioClave = true
        onGuardar()
    }

    fun limpiarGuardado() = _formulario.update { it.copy(guardado = false) }

    private fun generarContrasenaAleatoria(longitud: Int = 8): String {
        val caracteres = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789"
        val random = SecureRandom()
        return (1..longitud)
            .map { caracteres[random.nextInt(caracteres.length)] }
            .joinToString("")
    }

    fun onGuardar() {
        val f = _formulario.value
        val esEdicion = f.esEdicion

        val correo = f.correo.trim().lowercase()
        val rutNormalizado = Validaciones.normalizarRut(f.rut)

        val errorNombre = if (!Validaciones.nombreValido(f.nombre)) "Usa solo letras (mínimo 2)" else null
        val errorApellido = if (!Validaciones.nombreValido(f.apellido)) "Usa solo letras (mínimo 2)" else null
        val errorRut = when {
            f.rut.isBlank() -> "Ingresa el RUT"
            rutNormalizado == null -> "RUT inválido (revisa el dígito verificador)"
            else -> null
        }
        val errorTelefono = if (!Validaciones.telefonoValido(f.telefono)) "Ingresa 9 dígitos, parte con 9 (Ej: 912345678)" else null
        val errorCorreo = when {
            correo.isBlank() -> "Ingresa el correo"
            !Validaciones.correoValido(correo) -> "Ingresa un correo válido"
            else -> null
        }
        val errorEmpresa = if (f.idEmpresa == 0L) "Elige la empresa" else null

        val hayError = listOf(
            errorNombre, errorApellido, errorRut, errorTelefono, errorCorreo, errorEmpresa
        ).any { it != null }

        if (hayError) {
            solicitarReinicioClave = false
            _formulario.update {
                it.copy(
                    errorNombre = errorNombre, errorApellido = errorApellido, errorRut = errorRut,
                    errorTelefono = errorTelefono, errorCorreo = errorCorreo, errorEmpresa = errorEmpresa
                )
            }
            return
        }

        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val correoRepetido = repository.existeCorreo(correo, f.idUsuario)
            val rutRepetido = repository.existeRut(rutNormalizado!!, f.idUsuario)
            if (correoRepetido || rutRepetido) {
                solicitarReinicioClave = false
                _formulario.update {
                    it.copy(
                        guardando = false,
                        errorCorreo = if (correoRepetido) "Ya existe un usuario con ese correo" else null,
                        errorRut = if (rutRepetido) "Ya existe un usuario con ese RUT" else null
                    )
                }
                return@launch
            }

            val original = if (esEdicion) repository.buscarUsuarioEntity(f.idUsuario) else null

            // Si es usuario nuevo o se pidió reiniciar clave, se genera una clave aleatoria oculta
            val generarNuevaClave = !esEdicion || solicitarReinicioClave
            val nuevaClave = if (generarNuevaClave) generarContrasenaAleatoria() else null

            val contrasenaFinal = nuevaClave ?: original?.contrasena ?: generarContrasenaAleatoria()
            val preguntaActual = if (original != null && !generarNuevaClave) original.preguntaSeguridad else ""
            val respuestaActual = if (original != null && !generarNuevaClave) original.respuestaSeguridad else ""

            val entidad = UsuarioEntity(
                idUsuario = f.idUsuario,
                nombre = f.nombre.trim(),
                apellido = f.apellido.trim(),
                correo = correo,
                contrasena = contrasenaFinal,
                idRol = f.idRol,
                fotoPerfil = original?.fotoPerfil,
                rut = rutNormalizado,
                telefono = "+56" + f.telefono,
                idEmpresa = f.idEmpresa,
                preguntaSeguridad = preguntaActual,
                respuestaSeguridad = respuestaActual
            )

            val exito = if (esEdicion) repository.actualizarUsuario(entidad) else repository.crearUsuario(entidad) > 0
            if (exito && nuevaClave != null) {
                claveGeneradaPendiente = nuevaClave
            }
            solicitarReinicioClave = false

            _formulario.update {
                it.copy(
                    guardando = false,
                    guardado = exito,
                    errorGeneral = if (exito) null else "No se pudo guardar el usuario"
                )
            }
        }
    }
}