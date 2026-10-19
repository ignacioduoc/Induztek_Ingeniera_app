package com.example.induztek_ingeniera_app.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
//  Color.kt  —  "PALETA DE COLORES" (equivale a las variables de CSS)
//  ---------------------------------------------------------------------
//  En CSS:        :root { --primario: #0B6E5F; }
//  En Compose:    val Primario = Color(0xFF0B6E5F)
//
//  Formato: 0xAARRGGBB  ->  AA = opacidad (FF = 100 %), luego rojo, verde, azul.
//  Regla de oro: NUNCA escribir colores sueltos en las pantallas;
//  siempre referenciarlos desde este archivo o desde MaterialTheme.
// =====================================================================

// --- Colores de marca (diseño propio) ---
val Primario = Color(0xFF0B6E5F)        // Verde petróleo principal
val PrimarioOscuro = Color(0xFF06392F)
val PrimarioClaro = Color(0xFFE1F1ED)
val Secundario = Color(0xFF1D4ED8)      // Azul
val Acento = Color(0xFFB45309)          // Ámbar (llamados a la acción)
val AcentoOscuro = Color(0xFF92400E)

// --- Neutros ---
val Blanco = Color(0xFFFFFFFF)
val FondoClaro = Color(0xFFF4F7F6)
val SuperficieClara = Color(0xFFFFFFFF)
val TextoOscuro = Color(0xFF10201B)
val FondoOscuro = Color(0xFF0F1715)
val SuperficieOscura = Color(0xFF18221F)
val TextoClaro = Color(0xFFE6EEEB)
val GrisDeshabilitado = Color(0xFF8A9B95)

// --- Colores de estado (mensajes al usuario) ---
val Exito = Color(0xFF15803D)           // Verde: operación correcta
val ExitoFondo = Color(0xFFDDF4E5)