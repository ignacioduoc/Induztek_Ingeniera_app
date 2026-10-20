package com.example.induztek_ingeniera_app.navigation

// =====================================================================
//  Rutas.kt  —  LAS "DIRECCIONES" DE CADA PANTALLA
//  ---------------------------------------------------------------------
//  Cada pantalla tiene un nombre único, como las URL de una web:
//      "login"   "inicio_tecnico"   "inicio_cliente"
//
//  ¿Por qué sealed class y no Strings sueltos?
//  Si escribo navigate("inicio_tecnco") por error, compila y la app falla.
//  Con Ruta.InicioTecnico.ruta el compilador revisa el nombre por nosotros.
// =====================================================================

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")                     // pantalla de inicio de sesión
    data object InicioTecnico : Ruta("inicio_tecnico")
    data object InicioCliente : Ruta("inicio_cliente")
}