package com.example.induztek_ingeniera_app.model


data class RestablecerUiState(

    // 1 = escribir el correo, 2 = responder la pregunta de seguridad, 3 = elegir la contraseña nueva
    val paso: Int = 1,

    val correo: String = "",
    val pregunta: String = "",          // la pregunta guardada de esa cuenta (paso 2)
    val respuesta: String = "",
    val nueva: String = "",
    val confirmar: String = "",

    val contrasenaVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorRespuesta: String? = null,
    val errorNueva: String? = null,
    val errorConfirmar: String? = null,

    val intentos: Int = 0,              // respuestas incorrectas seguidas

    val cambioExitoso: Boolean = false
)