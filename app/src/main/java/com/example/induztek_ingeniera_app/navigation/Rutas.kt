package com.example.induztek_ingeniera_app.navigation

import android.net.Uri


sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")                     // pantalla de inicio de sesión

    data object RestablecerContrasena : Ruta("restablecer_contrasena")

    data object InicioTecnico : Ruta("inicio_tecnico")
    data object InicioCliente : Ruta("inicio_cliente")

    data object Perfil : Ruta("perfil")

    data object NuevoProtocolo : Ruta("nuevo_protocolo/{idEquipo}") {
        fun crear(idEquipo: Long) = "nuevo_protocolo/$idEquipo"
    }

    data object DetalleProtocolo : Ruta("detalle_protocolo/{idProtocolo}") {
        fun crear(idProtocolo: Long) = "detalle_protocolo/$idProtocolo"
    }

    data object Historial : Ruta("historial/{idEquipo}") {
        fun crear(idEquipo: Long) = "historial/$idEquipo"
    }


    data object Mapa : Ruta("mapa/{codigo}/{instalacion}/{lat}/{lng}") {
        fun crear(codigo: String, instalacion: String, lat: Double, lng: Double) =
            "mapa/${Uri.encode(codigo)}/${Uri.encode(instalacion)}/$lat/$lng"
    }
}