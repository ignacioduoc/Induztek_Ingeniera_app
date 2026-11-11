package com.example.induztek_ingeniera_app.model


// Todo lo que muestra el formulario de usuario: lo escrito, los errores por campo y el estado de guardado
data class FormularioUsuarioState(
    val idUsuario: Long = 0,                    // 0 = usuario nuevo; distinto de 0 = editando
    val nombre: String = "",
    val apellido: String = "",
    val rut: String = "",
    val telefono: String = "",                  // solo los 9 dígitos; el "+56" va fijo
    val correo: String = "",
    val idRol: Long = Rol.ID_TECNICO,
    val idEmpresa: Long = 0,
    val contrasena: String = "",
    val contrasenaVisible: Boolean = false,
    val pregunta: String = "",
    val respuesta: String = "",

    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorRut: String? = null,
    val errorTelefono: String? = null,
    val errorCorreo: String? = null,
    val errorEmpresa: String? = null,
    val errorContrasena: String? = null,
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null,
    val errorGeneral: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
) {
    val esEdicion: Boolean get() = idUsuario != 0L
}