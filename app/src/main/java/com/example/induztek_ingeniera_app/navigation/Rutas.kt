package com.example.induztek_ingeniera_app.navigation

import android.net.Uri


sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")                     // pantalla de inicio de sesión

    data object RestablecerContrasena : Ruta("restablecer_contrasena")
    data object ConfigurarCuenta : Ruta("configurar_cuenta")   // primer ingreso: contraseña nueva y pregunta

    data object InicioTecnico : Ruta("inicio_tecnico")
    data object InicioCliente : Ruta("inicio_cliente")
    data object InicioAdministrador : Ruta("inicio_administrador")

    // Gestión del administrador (una opción del menú por cada una)
    data object AdminUsuarios : Ruta("admin_usuarios")
    data object AdminUsuarioForm : Ruta("admin_usuario_form")      // crear y editar usuario
    data object AdminEquipos : Ruta("admin_equipos")
    data object AdminEquipoForm : Ruta("admin_equipo_form")        // crear y editar equipo
    data object AdminEmpresas : Ruta("admin_empresas")
    data object AdminEmpresaForm : Ruta("admin_empresa_form")      // crear y editar empresa

    // Consulta (sin editar): "Mis equipos" del cliente y "Empresas" del técnico / "Mi empresa" del cliente
    data object MisEquipos : Ruta("mis_equipos")
    data object EmpresasConsulta : Ruta("empresas_consulta")

    data object Perfil : Ruta("perfil")

    data object AgregarEquipo : Ruta("agregar_equipo")

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