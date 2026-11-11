package com.example.induztek_ingeniera_app.model


// Todo lo que muestra la pantalla "Editar perfil": lo escrito, los errores por campo y el estado de guardado.
// Sirve para los tres roles (técnico, cliente y administrador).
data class FormularioPerfilState(
    val idUsuario: Long = 0,

    // Editables
    val nombre: String = "",
    val apellido: String = "",
    val telefono: String = "",                  // solo los 9 dígitos; el "+56" va fijo
    val pregunta: String = "",
    val respuesta: String = "",
    val contrasenaActual: String = "",
    val contrasenaNueva: String = "",
    val contrasenasVisibles: Boolean = false,

    // Solo lectura (los cambia el administrador)
    val rut: String = "",
    val correo: String = "",
    val empresa: String = "",

    // La pregunta que tenía guardada: si se cambia, hay que escribir una respuesta nueva
    val preguntaOriginal: String = "",

    val errorNombre: String? = null,
    val errorApellido: String? = null,
    val errorTelefono: String? = null,
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null,
    val errorContrasenaActual: String? = null,
    val errorContrasenaNueva: String? = null,
    val errorGeneral: String? = null,

    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)