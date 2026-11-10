package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "empresas",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class EmpresaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_empresa") val idEmpresa: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val nombre: String,
    @ColumnInfo(defaultValue = "''") val direccion: String = "",
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val logo: ByteArray? = null
)