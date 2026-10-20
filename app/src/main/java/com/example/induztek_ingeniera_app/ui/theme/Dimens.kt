package com.example.induztek_ingeniera_app.ui.theme

import androidx.compose.ui.unit.dp

// =====================================================================
//  Dimens.kt  —  MEDIDAS REUTILIZABLES (márgenes, radios, alturas)
//  ---------------------------------------------------------------------
//  En CSS:      --espacio-m: 16px;   border-radius: 12px;
//  En Compose:  Dimens.espacioMedio  (en "dp": píxeles independientes
//               de la densidad; se ven del mismo tamaño en todo celular)
//
//  "object" en Kotlin = una única instancia (singleton): sirve como
//  contenedor de constantes que usamos desde cualquier archivo.
// =====================================================================

object Dimens {
    // Espaciados (margin / padding / gap)
    val espacioMini = 4.dp
    val espacioChico = 8.dp
    val espacioMedio = 16.dp
    val espacioGrande = 24.dp
    val espacioPantalla = 20.dp

    // Botones
    val alturaBoton = 52.dp
    val radioBoton = 14.dp
    val bordeBoton = 2.dp
    val elevacionBoton = 4.dp

    // Campos de texto
    val radioCampo = 12.dp

    // Tarjetas (Card)
    val radioTarjeta = 20.dp
    val elevacionTarjeta = 2.dp

    // Imágenes
    val altoBanner = 200.dp
    val altoFoto = 180.dp       // CAMBIO (Foto de evidencia): antes se llamaba altoPortada
    val tamanoAvatar = 120.dp
    val bordeFoto = 4.dp        // CAMBIO (Foto de evidencia): antes se llamaba bordeAvatar
    val tamanoIcono = 20.dp
}