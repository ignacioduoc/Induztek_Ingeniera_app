package com.example.induztek_ingeniera_app.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream


/**
 * Lee una imagen de la galería, la achica para que su lado mayor mida como máximo [maxDim] píxeles
 * y la devuelve como PNG (el PNG conserva la transparencia, que sirve para logos).
 * Devuelve null si no se pudo leer.
 */
fun leerImagenComoPng(context: Context, uri: Uri, maxDim: Int = 256): ByteArray? {
    return try {
        val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null

        val mayor = maxOf(original.width, original.height)
        val escala = if (mayor > maxDim) maxDim.toFloat() / mayor else 1f
        val ancho = (original.width * escala).toInt().coerceAtLeast(1)
        val alto = (original.height * escala).toInt().coerceAtLeast(1)
        val reducida = Bitmap.createScaledBitmap(original, ancho, alto, true)

        val salida = ByteArrayOutputStream()
        reducida.compress(Bitmap.CompressFormat.PNG, 100, salida)
        salida.toByteArray()
    } catch (e: Exception) {
        null
    }
}