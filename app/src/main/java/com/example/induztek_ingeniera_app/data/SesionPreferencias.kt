package com.example.induztek_ingeniera_app.data

import android.content.Context
import androidx.core.content.edit

// =====================================================================
//  SesionPreferencias.kt
//  ---------------------------------------------------------------------
//  SharedPreferences es el "localStorage" de Android:
//  guarda pares CLAVE -> VALOR simples (String, Int, Long, Boolean...).
//
//      Web (JavaScript)                    Android (Kotlin)
//      localStorage.setItem("id", 3)  ->   prefs.edit { putLong("id", 3) }
//      localStorage.getItem("id")     ->   prefs.getLong("id", -1)
//      localStorage.removeItem("id")  ->   prefs.edit { remove("id") }
//
//  Se guarda en un archivo XML:
//      /data/data/com.example.induztek_ingeniera_app/shared_prefs/sesion.xml
//
//  ¿SQLite o SharedPreferences?
//   - Muchos registros con la misma estructura (protocolos) -> SQLite
//   - Un dato suelto / configuración (quién inició sesión)  -> SharedPreferences
// =====================================================================

class SesionPreferencias(context: Context) {

    // Abre (o crea) el archivo "sesion.xml".
    // MODE_PRIVATE = solo esta app puede leerlo.
    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    // Guarda el id del usuario que inició sesión.
    // edit { } abre el editor y al terminar llama a apply() (guarda en segundo plano).
    fun guardar(idUsuario: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idUsuario) }
    }

    // Lee el id guardado. Si la clave no existe devuelve -1.
    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    // Borra la clave -> al reabrir la app no hay sesión.
    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}