package com.example.induztek_ingeniera_app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.screens.DetalleProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.HistorialScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioClienteScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
import com.example.induztek_ingeniera_app.ui.screens.MapaTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.NuevoProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.PerfilScreen
import com.example.induztek_ingeniera_app.ui.screens.RestablecerContrasenaScreen
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel

@Composable
fun AppNavegacion(
    loginViewModel: LoginViewModel,
    protocolosViewModel: ProtocolosViewModel,
    restablecerViewModel: RestablecerContrasenaViewModel     // NUEVO (Paso 16)
) {
    val navController = rememberNavController()

    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()
    val protocolos by protocolosViewModel.protocolos.collectAsStateWithLifecycle()

    val rutaInicial = remember { rutaDeInicio(usuarioActual) }

    NavHost(navController = navController, startDestination = rutaInicial) {

        composable(Ruta.Login.ruta) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = { usuario ->

                    protocolosViewModel.registrarIngreso(usuario)
                    navController.navigate(rutaDeInicio(usuario)) {

                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },

                onOlvideContrasena = {
                    restablecerViewModel.limpiar()
                    navController.navigate(Ruta.RestablecerContrasena.ruta)
                }
            )
        }


        composable(Ruta.RestablecerContrasena.ruta) {
            RestablecerContrasenaScreen(
                viewModel = restablecerViewModel,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.InicioTecnico.ruta) {
            InicioTecnicoScreen(
                usuario = usuarioActual,
                equipos = equipos,

                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },

                onNuevaPrueba = { idEquipo -> navController.navigate(Ruta.NuevoProtocolo.crear(idEquipo)) },

                onVerHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },

                onVerMapa = { lat, lng, codigo, instalacion ->
                    navController.navigate(Ruta.Mapa.crear(codigo, instalacion, lat, lng))
                },
                onGuardarUbicacion = { idEquipo, direccion, lat, lng ->
                    protocolosViewModel.actualizarUbicacionEquipo(idEquipo, direccion, lat, lng)
                },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }       // borra TODO el historial
                        launchSingleTop = true
                    }
                }
            )
        }


        composable(
            route = Ruta.NuevoProtocolo.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
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
                protocolos = protocolos,

                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },

                onVerProtocolo = { idProtocolo -> navController.navigate(Ruta.DetalleProtocolo.crear(idProtocolo)) }
            )
        }


        composable(
            route = Ruta.Mapa.ruta,
            arguments = listOf(
                navArgument("codigo") { type = NavType.StringType },
                navArgument("instalacion") { type = NavType.StringType },
                navArgument("lat") { type = NavType.StringType },
                navArgument("lng") { type = NavType.StringType }
            )
        ) { entrada ->
            val codigo = Uri.decode(entrada.arguments?.getString("codigo") ?: "")
            val instalacion = Uri.decode(entrada.arguments?.getString("instalacion") ?: "")
            val lat = entrada.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
            val lng = entrada.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0

            MapaTecnicoScreen(
                codigoEquipo = codigo,
                instalacion = instalacion,
                latitud = lat,
                longitud = lng,
                onVolver = { navController.navigateUp() }
            )
        }


        composable(
            route = Ruta.DetalleProtocolo.ruta,
            arguments = listOf(navArgument("idProtocolo") { type = NavType.LongType })
        ) { entrada ->
            val idProtocolo = entrada.arguments?.getLong("idProtocolo") ?: 0L

            var protocoloState by remember(idProtocolo) { mutableStateOf<Protocolo?>(null) }
            LaunchedEffect(idProtocolo) {
                protocolosViewModel.obtenerProtocolo(idProtocolo) { resultado ->
                    protocoloState = resultado
                }
            }

            protocoloState?.let { protocolo ->
                DetalleProtocoloScreen(
                    protocolo = protocolo,
                    onFormalizar = {
                        protocolosViewModel.formalizar(protocolo.id)                 // UPDATE
                        navController.navigateUp()
                    },

                    onVerHistorial = { navController.navigate(Ruta.Historial.crear(protocolo.equipoId)) },
                    onVolver = { navController.navigateUp() }
                )
            }
        }


        composable(
            route = Ruta.Historial.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }


            val historialFlow = remember(idEquipo) { protocolosViewModel.historialDe(idEquipo) }
            val historial by historialFlow.collectAsStateWithLifecycle()

            if (equipo != null) {
                HistorialScreen(
                    equipo = equipo,
                    historial = historial,
                    onVolver = { navController.navigateUp() }
                )
            }
        }


        composable(Ruta.Perfil.ruta) {
            PerfilScreen(
                usuario = usuarioActual,
                onGuardar = { nombre, correo, fotoPerfil ->
                    protocolosViewModel.actualizarPerfil(nombre, correo, fotoPerfil)     // UPDATE
                },
                onVolver = { navController.navigateUp() }
            )
        }
    }
}


private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta                       // sin sesión -> login
    usuario.rolId == Rol.ID_TECNICO -> Ruta.InicioTecnico.ruta
    else -> Ruta.InicioCliente.ruta
}