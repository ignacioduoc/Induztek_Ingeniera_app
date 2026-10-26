package com.example.induztek_ingeniera_app.model

// =====================================================================
//  Equipo.kt  —  una fila de la tabla "equipos"
//  ---------------------------------------------------------------------
//  Es el equipo eléctrico que el técnico prueba en terreno
//  (por ejemplo, TR-0142 · Transformador 500 kVA).
//  Cada equipo es de UN cliente; un cliente tiene MUCHOS equipos (1 a N).
// =====================================================================

data class Equipo(
    val codigo: String,
    val tipo: String,
    val instalacion: String,
    val clienteId: Long = 0,
    val id: Long = 0,
    val empresaCliente: String = "",
    // Agregar estos tres campos:
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)