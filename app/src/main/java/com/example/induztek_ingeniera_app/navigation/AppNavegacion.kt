package com.example.induztek_ingeniera_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.screens.DetalleProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioClienteScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
import com.example.induztek_ingeniera_app.ui.screens.NuevoProtocoloScreen
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel

// =====================================================================
//  AppNavegacion.kt  —  EL "MAPA" DE LA APP
//  ---------------------------------------------------------------------
//   NavController -> el "conductor": sabe dónde estamos y guarda el
//                    HISTORIAL (back stack) para volver atrás.
//   NavHost       -> el "contenedor": dibuja la pantalla de la ruta actual.
//   composable()  -> registra una ruta y QUÉ pantalla mostrar en ella.
//
//  Regla: las pantallas NO reciben el NavController, solo lambdas.
//
//  Aquí se crea el ViewModel COMPARTIDO. Está FUERA del NavHost,
//  por eso sobrevive a los cambios de pantalla.
//
//  Navegación por ROL: después del login, el técnico va a "inicio_tecnico"
//  y el cliente a "inicio_cliente". Si quedó una sesión guardada, la app
//  parte directo en la pantalla de inicio de ese usuario.
//
//  Dos formas de pasar datos en esta app (igual que en la app del profe):
//    1) ViewModel compartido -> todas las pantallas (usuarioActual, equipos, protocolos)
//    2) Argumento de ruta    -> Equipos -> Nuevo protocolo (el id del equipo)
//                               Mis protocolos -> Detalle (el id del protocolo)
// =====================================================================

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    // ViewModel compartido y lo que muestran las pantallas
    val protocolosViewModel: ProtocolosViewModel = viewModel()
    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()
    val protocolos by protocolosViewModel.protocolos.collectAsStateWithLifecycle()

    // La pantalla inicial se decide UNA sola vez, al abrir la app
    val rutaInicial = remember { rutaDeInicio(usuarioActual) }

    NavHost(navController = navController, startDestination = rutaInicial) {

        composable(Ruta.Login.ruta) {
            LoginScreen(
                onLoginExitoso = { usuario ->
                    // 1) guardar en el compartido  2) ir al inicio según el rol
                    protocolosViewModel.registrarIngreso(usuario)
                    navController.navigate(rutaDeInicio(usuario)) {
                        // Saca el login del historial: con "atrás" ya no se vuelve al login
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.InicioTecnico.ruta) {
            InicioTecnicoScreen(
                usuario = usuarioActual,
                equipos = equipos,                  // la lista viene del compartido
                // "nuevo_protocolo/1" -> el id del equipo VIAJA en la ruta
                onNuevaPrueba = { idEquipo -> navController.navigate(Ruta.NuevoProtocolo.crear(idEquipo)) },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.InicioTecnico.ruta) { inclusive = true }
                    }
                }
            )
        }

        // Ruta con argumento: navArgument declara el "hueco" {idEquipo} y su tipo (Long);
        // al llegar se LEE desde la entrada del historial.
        composable(
            route = Ruta.NuevoProtocolo.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L     // dato recibido
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }
            val tecnico = usuarioActual
            if (equipo != null && tecnico != null) {
                NuevoProtocoloScreen(
                    equipo = equipo,
                    tecnicoId = tecnico.id,
                    onGuardar = { protocolo -> protocolosViewModel.guardarProtocolo(protocolo) },  // INSERT
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(Ruta.InicioCliente.ruta) {
            InicioClienteScreen(
                usuario = usuarioActual,
                protocolos = protocolos,            // la lista viene del compartido
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.InicioCliente.ruta) { inclusive = true }
                    }
                },
                // "detalle_protocolo/5" -> el id del protocolo VIAJA en la ruta
                onVerProtocolo = { idProtocolo -> navController.navigate(Ruta.DetalleProtocolo.crear(idProtocolo)) }
            )
        }

        // Igual que la ruta "Editar" del profe: se lee el id, se busca en la BD y,
        // después del UPDATE, se vuelve atrás a la lista (que ya se recargó).
        composable(
            route = Ruta.DetalleProtocolo.ruta,
            arguments = listOf(navArgument("idProtocolo") { type = NavType.LongType })
        ) { entrada ->
            val idProtocolo = entrada.arguments?.getLong("idProtocolo") ?: 0L
            val protocolo = remember(idProtocolo) { protocolosViewModel.obtenerProtocolo(idProtocolo) }
            if (protocolo != null) {
                DetalleProtocoloScreen(
                    protocolo = protocolo,
                    onFormalizar = {
                        protocolosViewModel.formalizar(protocolo.id)              // UPDATE
                        navController.navigateUp()
                    },
                    onVolver = { navController.navigateUp() }
                )
            }
        }
    }
}

// Navegación por rol: decide la pantalla de inicio según quién entró.
private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta                       // sin sesión -> login
    usuario.rolId == Rol.ID_TECNICO -> Ruta.InicioTecnico.ruta
    else -> Ruta.InicioCliente.ruta
}