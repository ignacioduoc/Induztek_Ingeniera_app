package com.example.induztek_ingeniera_app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================
//  Formatos.kt  —  CÓMO SE MUESTRAN LOS DATOS EN PANTALLA
//  ---------------------------------------------------------------------
//  En la BD la fecha se guarda como número (milisegundos) y las mediciones
//  como REAL (13.1). Aquí se convierten a texto al estilo de Chile:
//      1776174300000 -> "14-04-2026 09:45"      13.1 -> "13,1"
//  Lo usan todas las pantallas (y el gráfico), así todo se ve igual.
// =====================================================================

// Chile: fecha día-mes-año y decimales con coma
private val chile = Locale.forLanguageTag("es-CL")

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", chile)
private val formatoFechaCorta = SimpleDateFormat("dd-MM-yy", chile)

fun formatearFecha(milisegundos: Long): String = formatoFecha.format(Date(milisegundos))

// Fecha corta para el gráfico (cabe bajo una barra): 1776174300000 -> "14-04-26"
fun formatearFechaCorta(milisegundos: Long): String = formatoFechaCorta.format(Date(milisegundos))

// MEDICIÓN con 1 decimal y coma: 13.1 -> "13,1"   ("%.1f" = número con 1 decimal)
fun formatearMedicion(valor: Double): String = String.format(chile, "%.1f", valor)