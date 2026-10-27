package com.example.induztek_ingeniera_app.model


enum class Resultado(val etiqueta: String) {
    APROBADO("Aprobado"),
    CON_OBSERVACION("Con observación"),
    RECHAZADO("Rechazado")
}

enum class EstadoProtocolo(val etiqueta: String) {
    REGISTRADO_EN_TERRENO("Registrado en terreno"),
    FORMALIZADO("Formalizado")
}

data class Protocolo(
    val equipoId: Long,
    val tecnicoId: Long,
    val fechaHora: Long,
    val tipoPrueba: String,


    val atBt: Double,
    val atTierra: Double,
    val btTierra: Double,

    val indicePolarizacion: Double,

    val resultado: Resultado,
    val observaciones: String,
    val estado: EstadoProtocolo = EstadoProtocolo.REGISTRADO_EN_TERRENO,
    val foto: ByteArray? = null,
    val id: Long = 0,

    val codigoEquipo: String = "",
    val tipoEquipo: String = "",
    val instalacion: String = "",
    val nombreTecnico: String = ""
)