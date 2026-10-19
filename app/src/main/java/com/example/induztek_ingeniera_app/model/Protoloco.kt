package com.example.induztek_ingeniera_app.model

// =====================================================================
//  Protocolo.kt  —  una fila de la tabla "protocolos"
//  ---------------------------------------------------------------------
//  Es el registro de UNA prueba: qué equipo, quién la hizo, cuándo,
//  las mediciones, el resultado y el estado del protocolo.
//  Un equipo tiene MUCHOS protocolos (su historial); cada protocolo es
//  de UN equipo (1 a N). Lo mismo con el técnico que lo registra.
// =====================================================================

/** Resultado de la prueba. "etiqueta" es el texto visible (opciones del GrupoRadio). */
enum class Resultado(val etiqueta: String) {
    APROBADO("Aprobado"),
    CON_OBSERVACION("Con observación"),
    RECHAZADO("Rechazado")
}

/** Estado del protocolo. Lo pide el caso: registrado en terreno / formalizado. */
enum class EstadoProtocolo(val etiqueta: String) {
    REGISTRADO_EN_TERRENO("Registrado en terreno"),
    FORMALIZADO("Formalizado")
}

data class Protocolo(
    // Claves foráneas -> qué equipo se probó y qué técnico hizo la prueba.
    val equipoId: Long,
    val tecnicoId: Long,
    // Fecha y hora en milisegundos (System.currentTimeMillis()).
    // SQLite no tiene tipo fecha: se guarda como número y se formatea al mostrarla.
    val fechaHora: Long,
    // Qué se probó: "Aislamiento 5 kV DC"
    val tipoPrueba: String,

    // MEDICIÓN: resistencia de aislamiento en GΩ entre cada par de puntos.
    val atBt: Double,
    val atTierra: Double,
    val btTierra: Double,
    // MEDICIÓN: índice de polarización (no tiene unidad).
    val indicePolarizacion: Double,

    val resultado: Resultado,
    val observaciones: String,
    // Todo protocolo nace "registrado en terreno"; pasa a "formalizado"
    // cuando el cliente da su conformidad.
    val estado: EstadoProtocolo = EstadoProtocolo.REGISTRADO_EN_TERRENO,
    // La foto de evidencia como BYTES (columna BLOB). null = sin foto.
    val foto: ByteArray? = null,
    // Clave primaria de la fila. 0 = "aún no guardado en la BD".
    val id: Long = 0,

    // Datos que NO son columnas de "protocolos": llegan gracias al JOIN.
    val codigoEquipo: String = "",
    val tipoEquipo: String = "",
    val instalacion: String = "",
    val nombreTecnico: String = ""
)