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
    val rolNombre: String = "",
    // Foto de perfil almacenada en formato BLOB (ByteArray)
    val fotoPerfil: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usuario

        if (id != other.id) return false
        if (nombre != other.nombre) return false
        if (correo != other.correo) return false
        if (empresa != other.empresa) return false
        if (rolId != other.rolId) return false
        if (rolNombre != other.rolNombre) return false
        if (fotoPerfil != null) {
            if (other.fotoPerfil == null) return false
            if (!fotoPerfil.contentEquals(other.fotoPerfil)) return false
        } else if (other.fotoPerfil != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nombre.hashCode()
        result = 31 * result + correo.hashCode()
        result = 31 * result + empresa.hashCode()
        result = 31 * result + rolId.hashCode()
        result = 31 * result + rolNombre.hashCode()
        result = 31 * result + (fotoPerfil?.contentHashCode() ?: 0)
        return result
    }
}