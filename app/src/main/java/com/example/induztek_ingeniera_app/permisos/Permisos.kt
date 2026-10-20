package com.example.induztek_ingeniera_app.permisos

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

// =====================================================================
//  Permisos.kt  —  AYUDA PARA LOS PERMISOS PELIGROSOS (cámara)
//  ---------------------------------------------------------------------
//  Los 4 pasos para usar una API protegida por un permiso PELIGROSO:
//
//   1) DECLARAR   <uses-permission> en AndroidManifest.xml
//   2) VERIFICAR  ¿ya está concedido?             -> tiene()   (este archivo)
//   3) PEDIR      mostrar el diálogo del sistema   -> NuevoProtocoloScreen (RequestPermission)
//   4) REACCIONAR concedido -> usar la API  /  rechazado -> avisar y ofrecer Ajustes
//
//  Si el usuario rechaza 2 veces, Android NO vuelve a mostrar el diálogo:
//  la única forma de concederlo es desde Ajustes -> abrirAjustes().
// =====================================================================

object Permisos {

    // Nombre del permiso de cámara (es igual en todas las versiones)
    val CAMARA: String = Manifest.permission.CAMERA

    // PASO 2 -> VERIFICAR. Devuelve true si el usuario ya lo concedió.
    // ContextCompat funciona igual en todas las versiones de Android.
    fun tiene(context: Context, permiso: String): Boolean =
        ContextCompat.checkSelfPermission(context, permiso) == PackageManager.PERMISSION_GRANTED

    // Abre la pantalla de Ajustes de ESTA app (Permisos, Almacenamiento...).
    // "package:com.example.induztek_ingeniera_app" le indica a Ajustes qué app mostrar.
    fun abrirAjustes(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}