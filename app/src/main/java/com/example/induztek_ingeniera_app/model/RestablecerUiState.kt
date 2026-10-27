package com.example.induztek_ingeniera_app.model


data class RestablecerUiState(

    val correo: String = "",
    val nueva: String = "",
    val confirmar: String = "",

    val contrasenaVisible: Boolean = false,

    val errorCorreo: String? = null,
    val errorNueva: String? = null,
    val errorConfirmar: String? = null,

    val cambioExitoso: Boolean = false
)