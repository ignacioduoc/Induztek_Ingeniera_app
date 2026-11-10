package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id_rol"],
            childColumns = ["id_rol"]
        ),
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id_empresa"],
            childColumns = ["id_empresa"]
        )
    ],
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["rut"], unique = true),
        Index(value = ["id_rol"]),
        Index(value = ["id_empresa"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario") val idUsuario: Long = 0,
    val nombre: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val correo: String,
    val contrasena: String,
    @ColumnInfo(name = "id_rol") val idRol: Long,
    @ColumnInfo(name = "foto_perfil", typeAffinity = ColumnInfo.BLOB) val fotoPerfil: ByteArray? = null,
    @ColumnInfo(defaultValue = "''") val apellido: String = "",
    val rut: String? = null,
    @ColumnInfo(defaultValue = "''") val telefono: String = "",
    @ColumnInfo(name = "id_empresa") val idEmpresa: Long? = null,
    @ColumnInfo(name = "pregunta_seguridad", defaultValue = "''") val preguntaSeguridad: String = "",
    @ColumnInfo(name = "respuesta_seguridad", defaultValue = "''") val respuestaSeguridad: String = ""
)