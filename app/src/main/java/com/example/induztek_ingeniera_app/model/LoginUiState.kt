package com.example.induztek_ingeniera_app.model

// =====================================================================
//  LoginUiState.kt  —  ESTADO DE LA PANTALLA DE INICIO DE SESIÓN
//  ---------------------------------------------------------------------
//  Todo lo que la pantalla necesita para dibujarse: lo escrito en cada
//  campo, si la contraseña se ve y los mensajes de error.
//  Es una data class INMUTABLE (val): para cambiarla se crea una copia
//  con .copy(...). Así Compose detecta el cambio y vuelve a dibujar.
// =====================================================================

data class LoginUiState(
    // Valores de los campos
    val correo: String = "",
    val contrasena: String = "",

    // Estado visual
    val contrasenaVisible: Boolean = false,

    // Errores de validación (null = sin error)
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    // Error cuando los campos están bien escritos, pero el usuario no existe en la BD
    val errorCredenciales: String? = null,

    // Resultado: el usuario que inició sesión (null = todavía no entra)
    val usuarioIngresado: Usuario? = null
)