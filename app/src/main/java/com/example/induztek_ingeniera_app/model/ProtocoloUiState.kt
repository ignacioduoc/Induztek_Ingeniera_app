package com.example.induztek_ingeniera_app.model


data class ProtocoloUiState(
    val fechaHora: Long = System.currentTimeMillis(),

    val tipoPrueba: String = "Aislamiento 5 kV DC",
    val tiposPrueba: List<String> = listOf(
        "Aislamiento 1 kV DC", "Aislamiento 2,5 kV DC", "Aislamiento 5 kV DC"
    ),
    val atBt: String = "",
    val atTierra: String = "",
    val btTierra: String = "",
    val indicePolarizacion: String = "",
    val resultado: Resultado = Resultado.APROBADO,
    val observaciones: String = "",


    val foto: ByteArray? = null,


    val errorAtBt: String? = null,
    val errorAtTierra: String? = null,
    val errorBtTierra: String? = null,
    val errorIndicePolarizacion: String? = null,
    val errorObservaciones: String? = null,

    val guardado: Boolean = false
)