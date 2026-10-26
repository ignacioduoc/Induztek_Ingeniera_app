package com.example.induztek_ingeniera_app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.screens.DetalleProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.HistorialScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioClienteScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
import com.example.induztek_ingeniera_app.ui.screens.MapaTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.NuevoProtocoloScreen
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel
import com.example.induztek_ingeniera_app.viewmodel.NuevoProtocoloViewModel
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel

@Composable
fun AppNavegacion(
    loginViewModel: LoginViewModel,
    protocolosViewModel: ProtocolosViewModel,
    nuevoProtocoloViewModel: NuevoProtocoloViewModel
) {
    val navController = rememberNavController()

    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()
    val protocolos by protocolosViewModel.protocolos.collectAsStateWithLifecycle()

    val rutaInicial = remember { rutaDeInicio(usuarioActual) }

    NavHost(navController = navController, startDestination = rutaInicial) {

        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = { usuario ->
                    protocolosViewModel.registrarIngreso(usuario)
                    navController.navigate(rutaDeInicio(usuario)) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("inicio_tecnico") {
            InicioTecnicoScreen(
                usuario = usuarioActual,
                equipos = equipos,
                onNuevaPrueba = { idEquipo -> navController.navigate("nuevo_protocolo/$idEquipo") },
                onVerHistorial = { idEquipo -> navController.navigate("historial/$idEquipo") },
                onVerMapa = { lat, lng, codigo, instalacion ->
                    // Codificar parámetros para evitar rupturas por '/' y espacios
                    val codigoEnc = Uri.encode(codigo)
                    val instalacionEnc = Uri.encode(instalacion)
                    navController.navigate("mapa/$codigoEnc/$instalacionEnc/$lat/$lng")
                },
                onGuardarUbicacion = { idEquipo, direccion, lat, lng ->
                    protocolosViewModel.actualizarUbicacionEquipo(idEquipo, direccion, lat, lng)
                },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "nuevo_protocolo/{idEquipo}",
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }
            val tecnico = usuarioActual

            if (equipo != null && tecnico != null) {
                NuevoProtocoloScreen(
                    equipo = equipo,
                    tecnicoId = tecnico.id,
                    onGuardar = { protocolo -> protocolosViewModel.guardarProtocolo(protocolo) },
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable("inicio_cliente") {
            InicioClienteScreen(
                usuario = usuarioActual,
                protocolos = protocolos,
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onVerProtocolo = { idProtocolo -> navController.navigate("detalle_protocolo/$idProtocolo") }
            )
        }

        // Definición de ruta con StringType para prevenir errores de ciberseguridad/parseo en los parámetros
        composable(
            route = "mapa/{codigo}/{instalacion}/{lat}/{lng}",
            arguments = listOf(
                navArgument("codigo") { type = NavType.StringType },
                navArgument("instalacion") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { entrada ->
            val codigoRaw = entrada.arguments?.getString("codigo") ?: ""
            val instalacionRaw = entrada.arguments?.getString("instalacion") ?: ""
            val latRaw = entrada.arguments?.getString("lat") ?: "0.0"
            val lngRaw = entrada.arguments?.getString("lng") ?: "0.0"

            val codigo = Uri.decode(codigoRaw)
            val instalacion = Uri.decode(instalacionRaw)
            val lat = latRaw.toDoubleOrNull() ?: 0.0
            val lng = lngRaw.toDoubleOrNull() ?: 0.0

            MapaTecnicoScreen(
                codigoEquipo = codigo,
                instalacion = instalacion,
                latitud = lat,
                longitud = lng,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(
            route = "detalle_protocolo/{idProtocolo}",
            arguments = listOf(navArgument("idProtocolo") { type = NavType.LongType })
        ) { entrada ->
            val idProtocolo = entrada.arguments?.getLong("idProtocolo") ?: 0L

            var protocoloState by remember(idProtocolo) { mutableStateOf<com.example.induztek_ingeniera_app.model.Protocolo?>(null) }
            LaunchedEffect(idProtocolo) {
                protocolosViewModel.obtenerProtocolo(idProtocolo) { resultado ->
                    protocoloState = resultado
                }
            }

            protocoloState?.let { protocolo ->
                DetalleProtocoloScreen(
                    protocolo = protocolo,
                    onFormalizar = {
                        protocolosViewModel.formalizar(protocolo.id)
                        navController.navigateUp()
                    },
                    onVerHistorial = { navController.navigate("historial/${protocolo.equipoId}") },
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        composable(
            route = "historial/{idEquipo}",
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }

            val historialState by protocolosViewModel.historialDe(idEquipo).collectAsState()

            if (equipo != null) {
                HistorialScreen(
                    equipo = equipo,
                    historial = historialState,
                    onVolver = { navController.navigateUp() }
                )
            }
        }
    }
}

private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> "login"
    usuario.rolId == Rol.ID_TECNICO -> "inicio_tecnico"
    else -> "inicio_cliente"
}