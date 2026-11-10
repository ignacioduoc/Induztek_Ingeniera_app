package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_cliente"]
        ),
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id_empresa"],
            childColumns = ["id_empresa"]
        )
    ],
    indices = [
        Index(value = ["codigo"], unique = true),
        Index(value = ["id_cliente"]),
        Index(value = ["id_empresa"])
    ]
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true)
    val id_equipo: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val codigo: String,
    val tipo: String,
    val instalacion: String,
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val id_cliente: Long,
    val id_empresa: Long? = null
)

// Equipo + su empresa + la dirección de la empresa + el nombre del cliente dueño
data class EquipoConDetalles(
    @Embedded val equipo: EquipoEntity,

    @ColumnInfo(name = "nombre_empresa") val nombreEmpresa: String,
    @ColumnInfo(name = "direccion_empresa") val direccionEmpresa: String,
    @ColumnInfo(name = "nombre_cliente") val nombreCliente: String
)