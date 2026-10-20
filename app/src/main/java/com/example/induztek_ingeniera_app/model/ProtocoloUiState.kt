package com.example.induztek_ingeniera_app.model

// =====================================================================
//  ProtocoloUiState.kt  —  ESTADO DEL FORMULARIO "NUEVO PROTOCOLO"
//  ---------------------------------------------------------------------
//  Todo lo que la pantalla necesita para dibujarse: lo escrito en cada
//  campo, las opciones elegidas y los mensajes de error.
//  Las mediciones se guardan como TEXTO mientras se escriben ("12," es un
//  texto válido a medio escribir); se convierten a número al guardar.
// =====================================================================

data class ProtocoloUiState(
    // Dato automático: el momento en que se abrió el formulario
    val fechaHora: Long = System.currentTimeMillis(),

    // Valores de los campos
    val tipoPrueba: String = "Aislamiento 5 kV DC",
    val tiposPrueba: List<String> = listOf(
        "Aislamiento 1 kV DC", "Aislamiento 2,5 kV DC", "Aislamiento 5 kV DC"
    ),
    val atBt: String = "",                          // MEDICIÓN
    val atTierra: String = "",                      // MEDICIÓN
    val btTierra: String = "",                      // MEDICIÓN
    val indicePolarizacion: String = "",            // MEDICIÓN
    val resultado: Resultado = Resultado.APROBADO,
    val observaciones: String = "",

    // Errores de validación (null = sin error)
    val errorAtBt: String? = null,                  // MEDICIÓN
    val errorAtTierra: String? = null,              // MEDICIÓN
    val errorBtTierra: String? = null,              // MEDICIÓN
    val errorIndicePolarizacion: String? = null,    // MEDICIÓN
    val errorObservaciones: String? = null,

    // Resultado: true cuando ya se guardó (se muestra el mensaje y se bloquea el botón)
    val guardado: Boolean = false
)