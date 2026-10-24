package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
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
    @ColumnInfo(name = "id_equipo") val idEquipo: Long = 0,
    val codigo: String,
    val tipo: String,
    val instalacion: String,
    @ColumnInfo(name = "id_cliente") val idCliente: Long
)
