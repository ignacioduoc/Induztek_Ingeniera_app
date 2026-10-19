package com.example.induztek_ingeniera_app.model

// =====================================================================
//  Usuario.kt  —  una fila de la tabla "usuarios"
//  ---------------------------------------------------------------------
//  Es quien inicia sesión: un técnico de Induztek o un cliente.
//  La contraseña NO viaja: solo se usa dentro de la base de datos
//  para comprobar el inicio de sesión.
// =====================================================================

data class Usuario(
    val nombre: String,
    val correo: String,
    // Empresa donde trabaja: "Induztek Ingeniería SpA" o la del cliente.
    val empresa: String,
    // Clave foránea -> qué rol tiene (Rol.ID_TECNICO o Rol.ID_CLIENTE).
    val rolId: Long,
    // Clave primaria de la fila. 0 = "aún no guardado en la BD".
    val id: Long = 0,
    // Nombre del rol. NO es columna de "usuarios": llega gracias al JOIN.
    val rolNombre: String = ""
)