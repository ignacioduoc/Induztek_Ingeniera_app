package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ProtocoloConDetalles(
    @Embedded val protocolo: ProtocoloEntity,

    @ColumnInfo(name = "codigo") val codigoEquipo: String,
    @ColumnInfo(name = "tipo") val tipoEquipo: String,
    @ColumnInfo(name = "instalacion") val instalacion: String,
    @ColumnInfo(name = "nombre_tecnico") val nombreTecnico: String
)
