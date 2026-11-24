package com.example.induztek_ingeniera_app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.induztek_ingeniera_app.ui.utils.cargarBitmapDesdeRuta
import com.example.induztek_ingeniera_app.ui.utils.guardarBytesEnCarpetaImagenes
import java.io.ByteArrayOutputStream

object ConvertidorImagen {

    private const val LADO_MAXIMO = 800
    private const val CALIDAD_JPEG = 85

    /**
     * Achica el Bitmap de la cámara, lo guarda en /files/imagenes/
     * y devuelve la ruta absoluta (String) para guardarla en Room.
     */
    fun guardarBitmapEnCarpeta(
        context: Context,
        bitmap: Bitmap,
        prefijo: String = "proto"
    ): String? {
        val bytes = aBytes(bitmap)
        return guardarBytesEnCarpetaImagenes(
            context = context,
            bytes = bytes,
            prefijo = prefijo,
            extension = "jpg"
        )
    }

    /**
     * Carga un Bitmap desde la ruta almacenada en Room.
     */
    fun desdeRuta(ruta: String?): Bitmap? = cargarBitmapDesdeRuta(ruta)

    // --- Métodos anteriores mantenidos temporalmente mientras migramos la UI ---

    fun aBytes(bitmap: Bitmap): ByteArray {
        val chico = achicar(bitmap)
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    fun aBitmap(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    private fun achicar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true
        )
    }
}