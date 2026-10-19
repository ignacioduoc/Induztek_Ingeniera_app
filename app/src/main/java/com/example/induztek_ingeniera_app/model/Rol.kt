package com.example.induztek_ingeniera_app.model

// =====================================================================
//  Rol.kt
//  ---------------------------------------------------------------------
//  Representa una fila de la tabla "roles".
//  Un rol tiene MUCHOS usuarios; cada usuario tiene UN rol (relación 1 a N).
// =====================================================================

data class Rol(
    val id: Long,
    val nombre: String
) {
    companion object {
        // Ids de los roles. Coinciden con los datos precargados en la BD:
        // 1 = Técnico, 2 = Cliente. Con ellos la app decide qué pantallas mostrar.
        const val ID_TECNICO = 1L
        const val ID_CLIENTE = 2L
    }
}