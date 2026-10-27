package com.example.induztek_ingeniera_app.model


data class Equipo(
    val codigo: String,
    val tipo: String,
    val instalacion: String,
    val clienteId: Long = 0,
    val id: Long = 0,
    val empresaCliente: String = "",

    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0
)