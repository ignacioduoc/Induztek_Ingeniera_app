package com.example.induztek_ingeniera_app.ui.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.induztek_ingeniera_app.R

/**
 * Abre la ubicación en la app de mapas del celular (Google Maps u otra).
 * Es gratis y no necesita clave de API: usa el esquema "geo:" y, si no hay app de mapas,
 * el enlace web oficial de Google Maps (https://developers.google.com/maps/documentation/urls).
 *
 * Si el equipo tiene coordenadas se usan ésas; si no, se busca por el texto de la dirección.
 */
fun abrirEnMapas(context: Context, latitud: Double, longitud: Double, direccion: String) {
    val tieneCoordenadas = latitud != 0.0 && longitud != 0.0

    if (!tieneCoordenadas && direccion.isBlank()) {
        Toast.makeText(context, context.getString(R.string.error_sin_ubicacion), Toast.LENGTH_SHORT).show()
        return
    }

    val consulta = if (tieneCoordenadas) "$latitud,$longitud" else Uri.encode(direccion)

    val geoUri = Uri.parse("geo:0,0?q=$consulta")
    val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$consulta")

    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
    } catch (e: Exception) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        } catch (e2: Exception) {
            Toast.makeText(context, context.getString(R.string.error_sin_app_mapas), Toast.LENGTH_LONG).show()
        }
    }
}