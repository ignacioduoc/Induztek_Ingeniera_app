package com.example.induztek_ingeniera_app.model

data class Usuario(
    val nombre: String,
    val correo: String,
    val empresa: String,
    val rolId: Long,
    val id: Long = 0,
    val rolNombre: String = "",
    val fotoPerfil: String? = null, // Ruta del archivo en /files/imagenes/
    val apellido: String = "",
    val rut: String = "",
    val telefono: String = "",
    val idEmpresa: Long = 0,
    val direccionEmpresa: String = "",
    // false = cuenta nueva o reiniciada: debe configurar su contraseña y su pregunta de seguridad al ingresar
    val tienePreguntaSeguridad: Boolean = true
) {
    val nombreCompleto: String
        get() = "$nombre $apellido".trim()
}