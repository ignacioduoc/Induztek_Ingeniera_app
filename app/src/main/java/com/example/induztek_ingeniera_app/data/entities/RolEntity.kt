package com.example.induztek_ingeniera_app.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_rol") val idRol: Long = 0,
    @ColumnInfo(name = "nombre_rol") val nombreRol: String
)