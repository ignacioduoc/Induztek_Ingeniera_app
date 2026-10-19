package com.example.induztek_ingeniera_app.model

// =====================================================================
//  Equipo.kt  —  una fila de la tabla "equipos"
//  ---------------------------------------------------------------------
//  Es el equipo eléctrico que el técnico prueba en terreno
//  (por ejemplo, TR-0142 · Transformador 500 kVA).
//  Cada equipo es de UN cliente; un cliente tiene MUCHOS equipos (1 a N).
// =====================================================================

data class Equipo(
    // Código que identifica al equipo en la planta: "TR-0142"
    val codigo: String,
    // Tipo de equipo: "Transformador 500 kVA"
    val tipo: String,
    // Instalación o ubicación del cliente: "Planta Lampa · S/E-02"
    val instalacion: String,
    // Clave foránea -> el usuario cliente dueño del equipo.
    val clienteId: Long,
    // Clave primaria de la fila. 0 = "aún no guardado en la BD".
    val id: Long = 0,
    // Empresa del cliente. NO es columna de "equipos": llega gracias al JOIN.
    val empresaCliente: String = ""
)