package com.example.induztek_ingeniera_app.ui.utils

import android.content.Context
import android.location.Geocoder
import org.osmdroid.util.GeoPoint
import java.util.Locale

fun buscarCoordenadasGratis(
    context: Context,
    direccion: String,
    onResult: (GeoPoint?) -> Unit
) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        @Suppress("DEPRECATION")
        val lista = geocoder.getFromLocationName(direccion, 1)
        if (!lista.isNullOrEmpty()) {
            val location = lista[0]
            onResult(GeoPoint(location.latitude, location.longitude))
        } else {
            onResult(null)
        }
    } catch (e: Exception) {
        onResult(null)
    }
}