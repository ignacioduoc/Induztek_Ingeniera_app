package com.example.induztek_ingeniera_app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "equipos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_cliente"]
        )
    ]
)
data class EquipoEntity(
    @PrimaryKey(autoGenerate = true)
    val id_equipo: Long = 0,
    val codigo: String,
    val tipo: String,
    val instalacion: String,
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val id_cliente: Long
)