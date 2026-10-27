package com.example.induztek_ingeniera_app.model

// =====================================================================
//  RestablecerUiState.kt  —  ESTADO DE LA PANTALLA "RESTABLECER CONTRASEÑA"
//  ---------------------------------------------------------------------
//  Igual que LoginUiState (y el FormularioUiState del profe): todo lo que
//  la pantalla necesita para dibujarse. Lo escrito en cada campo, si las
//  contraseñas se ven, los mensajes de error y el resultado.
//  Es una data class INMUTABLE (val): para cambiarla se crea una copia
//  con .copy(...). Así Compose detecta el cambio y vuelve a dibujar.
// =====================================================================

data class RestablecerUiState(
    // Valores de los campos
    val correo: String = "",
    val nueva: String = "",          // la contraseña nueva
    val confirmar: String = "",      // la misma contraseña, escrita otra vez

    // Estado visual: UN solo valor para los dos campos de contraseña
    // (al tocar cualquiera de los dos "ojos" se muestran ambas)
    val contrasenaVisible: Boolean = false,

    // Errores de validación (null = sin error)
    val errorCorreo: String? = null,
    val errorNueva: String? = null,
    val errorConfirmar: String? = null,

    // Resultado: true cuando la BD ya guardó la contraseña nueva
    val cambioExitoso: Boolean = false
)