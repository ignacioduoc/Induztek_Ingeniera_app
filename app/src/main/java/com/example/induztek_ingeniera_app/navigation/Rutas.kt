package com.example.induztek_ingeniera_app.navigation

import android.net.Uri

// =====================================================================
//  Rutas.kt  —  LAS "DIRECCIONES" DE CADA PANTALLA
//  ---------------------------------------------------------------------
//  Cada pantalla tiene un nombre único, como las URL de una web:
//      "login"   "restablecer_contrasena"   "inicio_tecnico"   "inicio_cliente"
//
//  ¿Por qué sealed class y no Strings sueltos?
//  Si escribo navigate("inicio_tecnco") por error, compila y la app falla.
//  Con Ruta.InicioTecnico.ruta el compilador revisa el nombre por nosotros.
//
//  Rutas CON ARGUMENTO: "nuevo_protocolo/{idEquipo}",
//  "detalle_protocolo/{idProtocolo}", "historial/{idEquipo}" y el mapa.
//  Lo que va entre { } es un "hueco" que se rellena al navegar:
//  "nuevo_protocolo/1", "detalle_protocolo/5", "historial/1".
//  Así el dato (qué equipo o qué protocolo) viaja DENTRO de la ruta.
// =====================================================================

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")                     // pantalla de inicio de sesión

    // NUEVO (Paso 16): se abre desde el login con "¿Olvidaste tu contraseña?"
    data object RestablecerContrasena : Ruta("restablecer_contrasena")

    data object InicioTecnico : Ruta("inicio_tecnico")
    data object InicioCliente : Ruta("inicio_cliente")

    // Editar perfil (lo agregó Mario): se abre desde el inicio del técnico o del cliente
    data object Perfil : Ruta("perfil")

    // crear(1) -> "nuevo_protocolo/1"
    data object NuevoProtocolo : Ruta("nuevo_protocolo/{idEquipo}") {
        fun crear(idEquipo: Long) = "nuevo_protocolo/$idEquipo"
    }

    // crear(5) -> "detalle_protocolo/5"  (igual que "editar/{id}" del profe)
    data object DetalleProtocolo : Ruta("detalle_protocolo/{idProtocolo}") {
        fun crear(idProtocolo: Long) = "detalle_protocolo/$idProtocolo"
    }

    // crear(1) -> "historial/1"  (el historial de UN equipo)
    data object Historial : Ruta("historial/{idEquipo}") {
        fun crear(idEquipo: Long) = "historial/$idEquipo"
    }

    // El mapa del equipo (lo agregó Mario). Lleva CUATRO datos en la ruta.
    // Uri.encode cambia los espacios y las "/" por códigos (%20, %2F) para que
    // no corten la ruta en partes de más:
    // crear("TR-0142", "Planta Lampa · S/E-02", -33.2845, -70.8752)
    //   -> "mapa/TR-0142/Planta%20Lampa%20%C2%B7%20S%2FE-02/-33.2845/-70.8752"
    data object Mapa : Ruta("mapa/{codigo}/{instalacion}/{lat}/{lng}") {
        fun crear(codigo: String, instalacion: String, lat: Double, lng: Double) =
            "mapa/${Uri.encode(codigo)}/${Uri.encode(instalacion)}/$lat/$lng"
    }
}