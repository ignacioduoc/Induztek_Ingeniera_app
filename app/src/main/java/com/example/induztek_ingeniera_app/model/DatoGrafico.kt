package com.example.induztek_ingeniera_app.model

/**
 * MODELO
 * Representa un dato que se puede graficar: una etiqueta y su valor.
 * Es el DatoGrafico del profe con un cambio: "valor" es Double (no Int)
 * porque las mediciones tienen decimales (13,6 GΩ).
 * En el historial: etiqueta = fecha de la prueba, valor = la medición.
 */
data class DatoGrafico(
    val etiqueta: String,
    val valor: Double           // CAMBIO (Decimales): en el del profe es Int
)