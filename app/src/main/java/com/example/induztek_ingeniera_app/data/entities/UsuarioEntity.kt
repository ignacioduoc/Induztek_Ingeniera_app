package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id_rol"],
            childColumns = ["id_rol"]
        )
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario") val idUsuario: Long = 0,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val empresa: String,
    @ColumnInfo(name = "id_rol") val idRol: Long,
    @ColumnInfo(name = "foto_perfil", typeAffinity = ColumnInfo.BLOB) val fotoPerfil: ByteArray? = null
)