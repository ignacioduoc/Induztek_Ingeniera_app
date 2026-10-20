package com.example.induztek_ingeniera_app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

// =====================================================================
//  ConvertidorImagen.kt  —  PREPARA LA FOTO PARA GUARDARLA Y MOSTRARLA
//  ---------------------------------------------------------------------
//  La foto de evidencia pasa por 3 formas distintas en esta app:
//
//   Cámara ──▶ Bitmap ──▶ ByteArray (JPEG) ──▶ columna BLOB "foto" de protocolos
//                               │
//                               └─▶ Bitmap ──▶ Image en la pantalla
//
//   Bitmap    = la imagen en memoria, pixel por pixel (pesada).
//   ByteArray = la imagen comprimida en JPEG (liviana): es lo que se guarda.
//
//  "object" = una sola instancia para toda la app (como una clase con métodos
//  estáticos en Java). Se usa así: ConvertidorImagen.aBytes(foto)
// =====================================================================

object ConvertidorImagen {

    // Tamaño máximo del lado más largo, en pixeles.
    // La foto de evidencia no necesita 4000 px: achicarla hace que pese pocos KB en la BD.
    private const val LADO_MAXIMO = 400

    // Calidad JPEG de 0 a 100. 80 = buen equilibrio peso / calidad.
    private const val CALIDAD_JPEG = 80

    // Bitmap -> ByteArray (JPEG achicado). Esto es lo que va a SQLite.
    fun aBytes(bitmap: Bitmap): ByteArray {
        val chico = achicar(bitmap)
        val salida = ByteArrayOutputStream()
        chico.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
        return salida.toByteArray()
    }

    // ByteArray -> Bitmap, para MOSTRAR la foto guardada
    fun aBitmap(bytes: ByteArray): Bitmap? =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    // Reduce el tamaño manteniendo la proporción (no deforma la foto)
    private fun achicar(bitmap: Bitmap): Bitmap {
        val ladoMayor = maxOf(bitmap.width, bitmap.height)
        if (ladoMayor <= LADO_MAXIMO) return bitmap
        val escala = LADO_MAXIMO.toFloat() / ladoMayor
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * escala).toInt(),
            (bitmap.height * escala).toInt(),
            true    // true = suavizado (filtro bilineal)
        )
    }
}