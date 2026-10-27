package com.example.induztek_ingeniera_app.model


data class Usuario(
    val nombre: String,
    val correo: String,
    val empresa: String,
    val rolId: Long,
    val id: Long = 0,
    val rolNombre: String = "",
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