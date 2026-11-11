package com.example.induztek_ingeniera_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.data.hashRespuestaSeguridad
import com.example.induztek_ingeniera_app.model.FormularioPerfilState
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class PerfilViewModel(private val repository: ProtocoloRepository) : ViewModel() {

    private val _formulario = MutableStateFlow(FormularioPerfilState())
    val formulario: StateFlow<FormularioPerfilState> = _formulario.asStateFlow()

    // Carga los datos del usuario que inició sesión. Las contraseñas y la respuesta quedan vacías.
    fun abrirPerfil(idUsuario: Long) {
        _formulario.value = FormularioPerfilState(idUsuario = idUsuario, cargando = true)
        viewModelScope.launch {
            val u = repository.buscarUsuarioEntity(idUsuario)
            if (u == null) {
                _formulario.update { it.copy(cargando = false, errorGeneral = "No se encontró tu perfil") }
                return@launch
            }
            val empresa = u.idEmpresa?.let { repository.buscarEmpresa(it) }
            _formulario.value = FormularioPerfilState(
                idUsuario = u.idUsuario,
                nombre = u.nombre,
                apellido = u.apellido,
                telefono = u.telefono.removePrefix("+56").filter { it.isDigit() },
                pregunta = u.preguntaSeguridad,
                preguntaOriginal = u.preguntaSeguridad,
                rut = u.rut ?: "",
                correo = u.correo,
                empresa = empresa?.nombre ?: ""
            )
        }
    }

    // Cada vez que se escribe en un campo, se borra su error
    fun onNombreCambia(v: String) = _formulario.update { it.copy(nombre = v, errorNombre = null) }
    fun onApellidoCambia(v: String) = _formulario.update { it.copy(apellido = v, errorApellido = null) }
    fun onTelefonoCambia(v: String) = _formulario.update { it.copy(telefono = v, errorTelefono = null) }
    fun onPreguntaCambia(v: String) = _formulario.update { it.copy(pregunta = v, errorPregunta = null, errorRespuesta = null) }
    fun onRespuestaCambia(v: String) = _formulario.update { it.copy(respuesta = v, errorRespuesta = null) }
    fun onContrasenaActualCambia(v: String) =
        _formulario.update { it.copy(contrasenaActual = v, errorContrasenaActual = null) }
    fun onContrasenaNuevaCambia(v: String) =
        _formulario.update { it.copy(contrasenaNueva = v, errorContrasenaNueva = null) }
    fun onContrasenaConfirmarCambia(v: String) =
        _formulario.update { it.copy(contrasenaConfirmar = v, errorContrasenaConfirmar = null) }
    fun onAlternarVisibilidad() = _formulario.update { it.copy(contrasenasVisibles = !it.contrasenasVisibles) }

    fun limpiarGuardado() = _formulario.update { it.copy(guardado = false) }

    /**
     * nuevaFoto: la foto ya comprimida si el usuario eligió una distinta; null = dejar la que tenía.
     * Nombre, apellido, teléfono y pregunta se guardan siempre. La respuesta y la contraseña
     * solo se cambian si se escribieron.
     */
    fun onGuardar(nuevaFoto: ByteArray?) {
        val f = _formulario.value

        // 1) Validaciones que no necesitan la base de datos
        val errorNombre = if (!Validaciones.nombreValido(f.nombre)) "Usa solo letras (mínimo 2)" else null
        val errorApellido = if (!Validaciones.nombreValido(f.apellido)) "Usa solo letras (mínimo 2)" else null
        val errorTelefono =
            if (!Validaciones.telefonoValido(f.telefono)) "Ingresa 9 dígitos, parte con 9 (Ej: 912345678)" else null

        val errorPregunta = if (f.pregunta.isBlank()) "Elige una pregunta de seguridad" else null
        // Si cambió la pregunta, la respuesta anterior ya no sirve: hay que escribir una nueva
        val preguntaCambio = f.pregunta != f.preguntaOriginal
        val errorRespuesta = when {
            f.respuesta.isBlank() && preguntaCambio -> "Escribe la respuesta de la nueva pregunta"
            f.respuesta.isNotBlank() && f.respuesta.trim().length < 2 -> "Escribe al menos 2 caracteres"
            else -> null
        }

        // La contraseña solo se cambia si escribió algo en alguno de los dos campos
        val quiereCambiarContrasena = f.contrasenaActual.isNotBlank() || f.contrasenaNueva.isNotBlank()
        val errorContrasenaActual =
            if (quiereCambiarContrasena && f.contrasenaActual.isBlank()) "Escribe tu contraseña actual" else null
        val errorContrasenaNueva = when {
            !quiereCambiarContrasena -> null
            f.contrasenaNueva.isBlank() -> "Escribe la contraseña nueva"
            f.contrasenaNueva.length < 6 -> "Mínimo 6 caracteres"
            f.contrasenaNueva == f.contrasenaActual -> "Debe ser distinta de la actual"
            else -> null
        }

        val hayError = listOf(
            errorNombre, errorApellido, errorTelefono, errorPregunta,
            errorRespuesta, errorContrasenaActual, errorContrasenaNueva
        ).any { it != null }

        if (hayError) {
            _formulario.update {
                it.copy(
                    errorNombre = errorNombre, errorApellido = errorApellido, errorTelefono = errorTelefono,
                    errorPregunta = errorPregunta, errorRespuesta = errorRespuesta,
                    errorContrasenaActual = errorContrasenaActual, errorContrasenaNueva = errorContrasenaNueva
                )
            }
            return
        }

        // 2) Se lee el usuario guardado: ahí se comprueba la contraseña actual
        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val original = repository.buscarUsuarioEntity(f.idUsuario)
            if (original == null) {
                _formulario.update { it.copy(guardando = false, errorGeneral = "No se encontró tu perfil") }
                return@launch
            }
            if (quiereCambiarContrasena && f.contrasenaActual != original.contrasena) {
                _formulario.update {
                    it.copy(guardando = false, errorContrasenaActual = "La contraseña actual no es correcta")
                }
                return@launch
            }

            // 3) Guardar. El correo, RUT, empresa y rol no se tocan: los cambia el administrador.
            val actualizado = original.copy(
                nombre = f.nombre.trim(),
                apellido = f.apellido.trim(),
                telefono = "+56" + f.telefono,
                preguntaSeguridad = f.pregunta,
                // La respuesta se guarda con hash (nunca en texto plano); si no escribió una nueva, queda la que había
                respuestaSeguridad = if (f.respuesta.isBlank()) original.respuestaSeguridad
                else hashRespuestaSeguridad(f.respuesta),
                contrasena = if (quiereCambiarContrasena) f.contrasenaNueva else original.contrasena,
                fotoPerfil = nuevaFoto ?: original.fotoPerfil
            )
            val exito = repository.actualizarUsuario(actualizado)
            _formulario.update {
                it.copy(
                    guardando = false,
                    guardado = exito,
                    // Si se guardó, la contraseña queda vacía para que no se reenvíe
                    contrasenaActual = if (exito) "" else it.contrasenaActual,
                    contrasenaNueva = if (exito) "" else it.contrasenaNueva,
                    errorGeneral = if (exito) null else "No se pudo guardar tu perfil"
                )
            }
        }
    }

    /**
     * "Configura tu cuenta" (primer ingreso o cuenta reiniciada por el administrador).
     * El usuario ya entró con la contraseña temporal, así que no se la pedimos otra vez:
     * debe escribir una contraseña nueva (distinta de la temporal) y elegir su pregunta de seguridad.
     */
    fun onConfigurarCuenta() {
        val f = _formulario.value

        val errorNueva = when {
            f.contrasenaNueva.isBlank() -> "Escribe la contraseña nueva"
            f.contrasenaNueva.length < 6 -> "Mínimo 6 caracteres"
            else -> null
        }
        val errorConfirmar = when {
            errorNueva != null -> null
            f.contrasenaConfirmar != f.contrasenaNueva -> "Las contraseñas no coinciden"
            else -> null
        }
        val errorPregunta = if (f.pregunta.isBlank()) "Elige una pregunta de seguridad" else null
        val errorRespuesta = if (f.respuesta.trim().length < 2) "Escribe al menos 2 caracteres" else null

        if (listOf(errorNueva, errorConfirmar, errorPregunta, errorRespuesta).any { it != null }) {
            _formulario.update {
                it.copy(
                    errorContrasenaNueva = errorNueva, errorContrasenaConfirmar = errorConfirmar,
                    errorPregunta = errorPregunta, errorRespuesta = errorRespuesta
                )
            }
            return
        }

        _formulario.update { it.copy(guardando = true, errorGeneral = null) }
        viewModelScope.launch {
            val original = repository.buscarUsuarioEntity(f.idUsuario)
            if (original == null) {
                _formulario.update { it.copy(guardando = false, errorGeneral = "No se encontró tu cuenta") }
                return@launch
            }
            // La contraseña nueva no puede ser la temporal que entregó el administrador
            if (f.contrasenaNueva == original.contrasena) {
                _formulario.update {
                    it.copy(guardando = false, errorContrasenaNueva = "Debe ser distinta de la temporal")
                }
                return@launch
            }

            val exito = repository.actualizarUsuario(
                original.copy(
                    contrasena = f.contrasenaNueva,
                    preguntaSeguridad = f.pregunta,
                    respuestaSeguridad = hashRespuestaSeguridad(f.respuesta)
                )
            )
            _formulario.update {
                it.copy(
                    guardando = false,
                    guardado = exito,
                    contrasenaNueva = "", contrasenaConfirmar = "", respuesta = "",
                    errorGeneral = if (exito) null else "No se pudo guardar tu configuración"
                )
            }
        }
    }
}