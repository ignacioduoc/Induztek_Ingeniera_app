package com.example.induztek_ingeniera_app.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private const val CARPETA_IMAGENES = "imagenes"

private fun obtenerCarpetaImagenes(context: Context): File {
    val carpeta = File(context.filesDir, CARPETA_IMAGENES)
    if (!carpeta.exists()) {
        carpeta.mkdirs()
    }
    return carpeta
}

/**
 * Lee una imagen de galería/cámara, la redimensiona a [maxDim] y la guarda
 * como archivo en /files/imagenes/, devolviendo su ruta absoluta (String).
 */
fun guardarImagenDesdeUri(
    context: Context,
    uri: Uri,
    prefijo: String = "img",
    maxDim: Int = 1024,
    formatoPng: Boolean = false
): String? {
    return try {
        val original = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
            ?: return null

        val mayor = maxOf(original.width, original.height)
        val escala = if (mayor > maxDim) maxDim.toFloat() / mayor else 1f
        val ancho = (original.width * escala).toInt().coerceAtLeast(1)
        val alto = (original.height * escala).toInt().coerceAtLeast(1)
        val reducida = Bitmap.createScaledBitmap(original, ancho, alto, true)

        val extension = if (formatoPng) "png" else "jpg"
        val archivo = File(obtenerCarpetaImagenes(context), "${prefijo}_${UUID.randomUUID()}.$extension")

        FileOutputStream(archivo).use { salida ->
            if (formatoPng) {
                reducida.compress(Bitmap.CompressFormat.PNG, 100, salida)
            } else {
                reducida.compress(Bitmap.CompressFormat.JPEG, 85, salida)
            }
        }
        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * Guarda un ByteArray (por ejemplo desde cámara o red) en /files/imagenes/
 * y devuelve su ruta absoluta.
 */
fun guardarBytesEnCarpetaImagenes(
    context: Context,
    bytes: ByteArray,
    prefijo: String = "img",
    extension: String = "jpg"
): String? {
    return try {
        val archivo = File(obtenerCarpetaImagenes(context), "${prefijo}_${UUID.randomUUID()}.$extension")
        FileOutputStream(archivo).use { salida ->
            salida.write(bytes)
        }
        archivo.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * Carga un Bitmap desde una ruta local o desde Base64 (fotos que vienen del servidor).
 */
fun cargarBitmapDesdeRuta(ruta: String?): Bitmap? {
    if (ruta.isNullOrBlank()) return null

    val archivo = File(ruta)
    if (archivo.exists()) return BitmapFactory.decodeFile(archivo.absolutePath)

    return try {
        val bytes = Base64.decode(ruta, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}

/**
 * Elimina el archivo físico de la carpeta imagenes si existe.
 */
fun eliminarImagenDeRuta(ruta: String?) {
    if (!ruta.isNullOrBlank()) {
        File(ruta).takeIf { it.exists() }?.delete()
    }
}

/**
 * Alias usado por los ViewModels para limpiar imágenes anteriores o temporales.
 */
fun eliminarImagenSiExiste(ruta: String?) {
    eliminarImagenDeRuta(ruta)
}

/**
 * Función anterior mantenida temporalmente durante la migración.
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