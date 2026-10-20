package com.example.induztek_ingeniera_app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =====================================================================
//  Formatos.kt  —  CÓMO SE MUESTRAN LOS DATOS EN PANTALLA
//  ---------------------------------------------------------------------
//  En la BD la fecha se guarda como número (milisegundos). Aquí se
//  convierte a texto para mostrarla: 1776174300000 -> "14-04-2026 09:45".
//  Lo usan todas las pantallas que muestran fechas.
// =====================================================================

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

fun formatearFecha(milisegundos: Long): String = formatoFecha.format(Date(milisegundos))