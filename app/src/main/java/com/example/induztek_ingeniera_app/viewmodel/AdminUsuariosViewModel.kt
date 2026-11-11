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


class AdminUsuariosViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    // ---------- Lista ----------

    // Une dos listas de Room (usuarios y empresas) para mostrar el nombre de la empresa de cada usuario.
    // Si se agrega o edita un usuario, la lista se actualiza sola.
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

    // Para la lista desplegable de empresas del formulario
    val empresas: StateFlow<List<EmpresaEntity>> = repository.obtenerEmpresas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun nombreDeRol(idRol: Long): String = when (idRol) {
        Rol.ID_TECNICO -> "Técnico"
        Rol.ID_CLIENTE -> "Cliente"
        Rol.ID_ADMINISTRADOR -> "Administrador"
        else -> "Usuario"
    }


    // ---------- Formulario (crear / editar) ----------

    private val _formulario = MutableStateFlow(FormularioUsuarioState())
    val formulario: StateFlow<FormularioUsuarioState> = _formulario.asStateFlow()

    // idUsuario = 0 -> formulario vacío para crear. Otro id -> carga ese usuario para editarlo.
    fun abrirFormulario(idUsuario: Long) {
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
                    // La contraseña queda vacía: si no se escribe, se conserva la actual
                )
            } else {
                _formulario.update { it.copy(cargando = false, errorGeneral = "No se encontró el usuario") }
            }
        }
    }

    // Cada vez que se escribe en un campo, se borra su error
    fun onNombreCambia(v: String) = _formulario.update { it.copy(nombre = v, errorNombre = null) }
    fun onApellidoCambia(v: String) = _formulario.update { it.copy(apellido = v, errorApellido = null) }
    fun onRutCambia(v: String) = _formulario.update { it.copy(rut = v, errorRut = null) }
    fun onTelefonoCambia(v: String) = _formulario.update { it.copy(telefono = v, errorTelefono = null) }
    fun onCorreoCambia(v: String) = _formulario.update { it.copy(correo = v, errorCorreo = null) }
    fun onRolCambia(v: Long) = _formulario.update { it.copy(idRol = v) }
    fun onEmpresaCambia(v: Long) = _formulario.update { it.copy(idEmpresa = v, errorEmpresa = null) }
    fun onContrasenaCambia(v: String) = _formulario.update { it.copy(contrasena = v, errorContrasena = null) }
    fun onAlternarVisibilidad() = _formulario.update { it.copy(contrasenaVisible = !it.contrasenaVisible) }

    fun limpiarGuardado() = _formulario.update { it.copy(guardado = false) }

    fun onGuardar() {
        val f = _formulario.value
        val esEdicion = f.esEdicion

        val correo = f.correo.trim().lowercase()
        val rutNormalizado = Validaciones.normalizarRut(f.rut)

        // 1) Validaciones que no necesitan la base de datos
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
        val errorContrasena = when {
            f.contrasena.isBlank() && !esEdicion -> "Ingresa la contraseña"
            f.contrasena.isNotBlank() && f.contrasena.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }

        val hayError = listOf(
            errorNombre, errorApellido, errorRut, errorTelefono, errorCorreo,
            errorEmpresa, errorContrasena
        ).any { it != null }

        if (hayError) {
            _formulario.update {
                it.copy(
                    errorNombre = errorNombre, errorApellido = errorApellido, errorRut = errorRut,
                    errorTelefono = errorTelefono, errorCorreo = errorCorreo, errorEmpresa = errorEmpresa,
                    errorContrasena = errorContrasena
                )
            }
            return
        }

        // 2) Correo y RUT repetidos (consulta a la base). Al editar se excluye al propio usuario.
        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val correoRepetido = repository.existeCorreo(correo, f.idUsuario)
            val rutRepetido = repository.existeRut(rutNormalizado!!, f.idUsuario)
            if (correoRepetido || rutRepetido) {
                _formulario.update {
                    it.copy(
                        guardando = false,
                        errorCorreo = if (correoRepetido) "Ya existe un usuario con ese correo" else null,
                        errorRut = if (rutRepetido) "Ya existe un usuario con ese RUT" else null
                    )
                }
                return@launch
            }

            // 3) Guardar. Al editar, lo que no se escribió (contraseña) y la foto se conservan.
            val original = if (esEdicion) repository.buscarUsuarioEntity(f.idUsuario) else null

            // La pregunta de seguridad la elige el propio usuario (no el administrador):
            //  - usuario nuevo: queda sin pregunta y se la pedirán en su primer ingreso;
            //  - edición sin cambiar la contraseña: se conserva la pregunta que ya tenía;
            //  - edición con contraseña nueva (cuenta reiniciada): se borra para que cree una nueva.
            val reinicioDeClave = esEdicion && f.contrasena.isNotBlank()
            val preguntaActual = if (original != null && !reinicioDeClave) original.preguntaSeguridad else ""
            val respuestaActual = if (original != null && !reinicioDeClave) original.respuestaSeguridad else ""
            val entidad = UsuarioEntity(
                idUsuario = f.idUsuario,
                nombre = f.nombre.trim(),
                apellido = f.apellido.trim(),
                correo = correo,
                contrasena = if (f.contrasena.isBlank() && original != null) original.contrasena else f.contrasena,
                idRol = f.idRol,
                fotoPerfil = original?.fotoPerfil,
                rut = rutNormalizado,
                telefono = "+56" + f.telefono,
                idEmpresa = f.idEmpresa,
                preguntaSeguridad = preguntaActual,
                respuestaSeguridad = respuestaActual
            )

            val exito = if (esEdicion) repository.actualizarUsuario(entidad) else repository.crearUsuario(entidad) > 0
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