package com.example.induztek_ingeniera_app.model


data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",

    val contrasenaVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorCredenciales: String? = null,

    val usuarioIngresado: Usuario? = null
)