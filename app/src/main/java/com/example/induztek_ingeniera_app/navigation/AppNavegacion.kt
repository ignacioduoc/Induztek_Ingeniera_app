package com.example.induztek_ingeniera_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.screens.InicioClienteScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
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
// =====================================================================

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    // ViewModel compartido y el usuario con sesión iniciada
    val protocolosViewModel: ProtocolosViewModel = viewModel()
    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()

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
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.InicioTecnico.ruta) { inclusive = true }
                    }
                }
            )
        }

        composable(Ruta.InicioCliente.ruta) {
            InicioClienteScreen(
                usuario = usuarioActual,
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(Ruta.InicioCliente.ruta) { inclusive = true }
                    }
                }
            )
        }
    }
}

// Navegación por rol: decide la pantalla de inicio según quién entró.
private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta                       // sin sesión -> login
    usuario.rolId == Rol.ID_TECNICO -> Ruta.InicioTecnico.ruta
    else -> Ruta.InicioCliente.ruta
}