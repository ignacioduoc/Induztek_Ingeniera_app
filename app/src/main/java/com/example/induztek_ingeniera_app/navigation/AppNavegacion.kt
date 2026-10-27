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
//  Los ViewModels que usan la BD llegan desde MainActivity (los crea la
//  Factory con el Repository). Viven mientras la app está abierta.
//
//  Navegación por ROL: después del login, el técnico va a "inicio_tecnico"
//  y el cliente a "inicio_cliente".
// =====================================================================

@Composable
fun AppNavegacion(
    loginViewModel: LoginViewModel,
    protocolosViewModel: ProtocolosViewModel,
    restablecerViewModel: RestablecerContrasenaViewModel     // NUEVO (Paso 16)
    // ARREGLO: ya no llega nuevoProtocoloViewModel (la pantalla crea el suyo)
) {
    val navController = rememberNavController()

    // Lo que muestran las pantallas viene del ViewModel compartido
    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()
    val protocolos by protocolosViewModel.protocolos.collectAsStateWithLifecycle()

    // La pantalla inicial se decide UNA sola vez, al abrir la app
    val rutaInicial = remember { rutaDeInicio(usuarioActual) }

    NavHost(navController = navController, startDestination = rutaInicial) {

        composable(Ruta.Login.ruta) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginExitoso = { usuario ->
                    // 1) guardar en el compartido  2) ir al inicio según el rol
                    protocolosViewModel.registrarIngreso(usuario)
                    navController.navigate(rutaDeInicio(usuario)) {
                        // Saca el login del historial: con "atrás" ya no se vuelve al login
                        popUpTo(Ruta.Login.ruta) { inclusive = true }
                    }
                },
                // NUEVO: primero se limpia el formulario (lo escrito la vez anterior)
                // y después se abre la pantalla. El login queda debajo en el historial.
                onOlvideContrasena = {
                    restablecerViewModel.limpiar()
                    navController.navigate(Ruta.RestablecerContrasena.ruta)
                }
            )
        }

        // NUEVO (Paso 16): con la flecha, con "atrás" o con "Volver al inicio de sesión"
        // se saca esta pantalla del historial y aparece de nuevo el login.
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
                // Editar perfil (Mario)
                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                // "nuevo_protocolo/1" -> el id del equipo VIAJA en la ruta
                onNuevaPrueba = { idEquipo -> navController.navigate(Ruta.NuevoProtocolo.crear(idEquipo)) },
                // Ícono del gráfico -> "historial/1"
                onVerHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },
                // Mapa del equipo: Ruta.Mapa.crear() codifica el texto (ver Rutas.kt)
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

        // Ruta con argumento: navArgument declara el "hueco" {idEquipo} y su tipo (Long);
        // al llegar se LEE desde la "entrada" (la pantalla guardada en el back stack).
        composable(
            route = Ruta.NuevoProtocolo.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }
            val tecnico = usuarioActual

            if (equipo != null && tecnico != null) {
                // ARREGLO: no se le pasa un ViewModel. La pantalla crea el suyo con
                // viewModel(): uno NUEVO cada vez que se abre (formulario limpio y
                // fecha y hora del momento), y se borra al salir de la pantalla.
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
                // Editar perfil (Mario)
                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // "detalle_protocolo/5" -> el id del protocolo VIAJA en la ruta
                onVerProtocolo = { idProtocolo -> navController.navigate(Ruta.DetalleProtocolo.crear(idProtocolo)) }
            )
        }

        // El mapa recibe 4 textos. Se vuelven a decodificar y las coordenadas se
        // convierten a número (si llegara algo raro, queda 0.0 en vez de caerse).
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

        // Detalle: se lee el protocolo de Room UNA vez (en una corrutina) y se dibuja
        // cuando llega. Después del UPDATE se vuelve a la lista, que se actualiza sola (Flow).
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
                    // El id del equipo viene dentro del protocolo (columna id_equipo)
                    onVerHistorial = { navController.navigate(Ruta.Historial.crear(protocolo.equipoId)) },
                    onVolver = { navController.navigateUp() }
                )
            }
        }

        // Historial de UN equipo. Llegan aquí el técnico (ícono del gráfico)
        // y el cliente (botón del detalle).
        composable(
            route = Ruta.Historial.ruta,
            arguments = listOf(navArgument("idEquipo") { type = NavType.LongType })
        ) { entrada ->
            val idEquipo = entrada.arguments?.getLong("idEquipo") ?: 0L
            val equipo = remember(idEquipo) { protocolosViewModel.obtenerEquipo(idEquipo) }

            // ARREGLO: remember(idEquipo) pide el Flow del historial UNA sola vez por equipo.
            // Antes historialDe() se llamaba en CADA redibujo: cada vez nacía un Flow nuevo
            // que partía vacío, y la pantalla saltaba sin parar entre "sin pruebas" y el gráfico.
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

        // Editar perfil (Mario): nombre, correo y foto del usuario que inició sesión
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

// Navegación por rol: decide la pantalla de inicio según quién entró.
private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta                       // sin sesión -> login
    usuario.rolId == Rol.ID_TECNICO -> Ruta.InicioTecnico.ruta
    else -> Ruta.InicioCliente.ruta
}