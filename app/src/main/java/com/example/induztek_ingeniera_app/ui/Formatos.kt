package com.example.induztek_ingeniera_app.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



private val chile = Locale.forLanguageTag("es-CL")

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", chile)
private val formatoFechaCorta = SimpleDateFormat("dd-MM-yy", chile)

fun formatearFecha(milisegundos: Long): String = formatoFecha.format(Date(milisegundos))


fun formatearFechaCorta(milisegundos: Long): String = formatoFechaCorta.format(Date(milisegundos))

fun formatearMedicion(valor: Double): String = String.format(chile, "%.1f", valor)