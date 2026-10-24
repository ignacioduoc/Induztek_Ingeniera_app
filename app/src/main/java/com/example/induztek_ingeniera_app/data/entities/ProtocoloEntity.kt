package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "protocolos",
    foreignKeys = [
        ForeignKey(
            entity = EquipoEntity::class,
            parentColumns = ["id_equipo"],
            childColumns = ["id_equipo"]
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_tecnico"]
        )
    ]
)
data class ProtocoloEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_protocolo") val idProtocolo: Long = 0,
    @ColumnInfo(name = "id_equipo") val idEquipo: Long,
    @ColumnInfo(name = "id_tecnico") val idTecnico: Long,
    @ColumnInfo(name = "fecha_hora") val fechaHora: Long,
    @ColumnInfo(name = "tipo_prueba") val tipoPrueba: String,
    @ColumnInfo(name = "at_bt") val atBt: Double,
    @ColumnInfo(name = "at_tierra") val atTierra: Double,
    @ColumnInfo(name = "bt_tierra") val btTierra: Double,
    @ColumnInfo(name = "indice_polarizacion") val indicePolarizacion: Double,
    val resultado: String, // Guardado como String del Enum (.name)
    val observaciones: String,
    val estado: String,    // Guardado como String del Enum (.name)
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val foto: ByteArray?,
    val sincronizado: Boolean = false // Tu flag de control offline-first
)
