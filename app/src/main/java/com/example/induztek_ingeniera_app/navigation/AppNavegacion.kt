package com.example.induztek_ingeniera_app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.screens.AdminEmpresaFormScreen
import com.example.induztek_ingeniera_app.ui.screens.AdminEmpresasScreen
import com.example.induztek_ingeniera_app.ui.screens.AdminEquipoFormScreen
import com.example.induztek_ingeniera_app.ui.screens.AdminEquiposScreen
import com.example.induztek_ingeniera_app.ui.screens.AdminUsuarioFormScreen
import com.example.induztek_ingeniera_app.ui.screens.AdminUsuariosScreen
import com.example.induztek_ingeniera_app.ui.screens.AgregarEquipoScreen
import com.example.induztek_ingeniera_app.ui.screens.DetalleProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.EmpresasConsultaScreen
import com.example.induztek_ingeniera_app.ui.screens.HistorialScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioAdministradorScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioClienteScreen
import com.example.induztek_ingeniera_app.ui.screens.InicioTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
import com.example.induztek_ingeniera_app.ui.screens.MapaTecnicoScreen
import com.example.induztek_ingeniera_app.ui.screens.MisEquiposScreen
import com.example.induztek_ingeniera_app.ui.screens.NuevoProtocoloScreen
import com.example.induztek_ingeniera_app.ui.screens.PerfilScreen
import com.example.induztek_ingeniera_app.ui.screens.RestablecerContrasenaScreen
import com.example.induztek_ingeniera_app.viewmodel.AdminEmpresasViewModel
import com.example.induztek_ingeniera_app.viewmodel.AdminEquiposViewModel
import com.example.induztek_ingeniera_app.viewmodel.AdminUsuariosViewModel
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel
import com.example.induztek_ingeniera_app.viewmodel.PerfilViewModel
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel

@Composable
fun AppNavegacion(
    loginViewModel: LoginViewModel,
    protocolosViewModel: ProtocolosViewModel,
    restablecerViewModel: RestablecerContrasenaViewModel,    // NUEVO (Paso 16)
    adminUsuariosViewModel: AdminUsuariosViewModel,         // Tarea 3: gestión de usuarios
    adminEmpresasViewModel: AdminEmpresasViewModel,         // Tarea 3: gestión de empresas
    adminEquiposViewModel: AdminEquiposViewModel,           // Tarea 3: gestión de equipos
    perfilViewModel: PerfilViewModel                        // Tarea 6: edición de perfil (todos los roles)
) {
    val navController = rememberNavController()

    val usuarioActual by protocolosViewModel.usuarioActual.collectAsStateWithLifecycle()
    val equipos by protocolosViewModel.equipos.collectAsStateWithLifecycle()
    val empresas by protocolosViewModel.empresas.collectAsStateWithLifecycle()
    val clientes by protocolosViewModel.clientes.collectAsStateWithLifecycle()
    val protocolos by protocolosViewModel.protocolos.collectAsStateWithLifecycle()
    val usuariosAdmin by adminUsuariosViewModel.usuarios.collectAsStateWithLifecycle()
    val empresasAdmin by adminEmpresasViewModel.empresas.collectAsStateWithLifecycle()
    val equiposAdmin by adminEquiposViewModel.equipos.collectAsStateWithLifecycle()

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
                onAgregarEquipo = { navController.navigate(Ruta.AgregarEquipo.ruta) },
                onIrEmpresas = { navController.navigate(Ruta.EmpresasConsulta.ruta) },

                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },

                onNuevaPrueba = { idEquipo -> navController.navigate(Ruta.NuevoProtocolo.crear(idEquipo)) },

                onVerHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },

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


        composable(Ruta.AgregarEquipo.ruta) {
            AgregarEquipoScreen(
                equipos = equipos,
                empresas = empresas,
                clientes = clientes,
                onGuardar = { codigo, tipo, instalacion, idEmpresa, idCliente, direccion, lat, lng, onResultado ->
                    protocolosViewModel.agregarEquipo(codigo, tipo, instalacion, idEmpresa, idCliente, direccion, lat, lng, onResultado)
                },
                onVolver = { navController.navigateUp() }
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
                onVerHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },
                onIrMisEquipos = { navController.navigate(Ruta.MisEquipos.ruta) },
                onIrMiEmpresa = { navController.navigate(Ruta.EmpresasConsulta.ruta) },
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


        // ---- Cliente: "Mis equipos" ----
        composable(Ruta.MisEquipos.ruta) {
            MisEquiposScreen(
                equipos = equipos,                       // para el cliente, solo los equipos que le pertenecen
                protocolos = protocolos,                 // para mostrar estado y técnico de la última prueba
                onVerUltimaPrueba = { idEquipo ->
                    // Reutiliza el detalle de protocolo: abre la prueba más reciente del equipo
                    val ultima = protocolos.filter { it.equipoId == idEquipo }.maxByOrNull { it.fechaHora }
                    navController.navigate(
                        if (ultima != null) Ruta.DetalleProtocolo.crear(ultima.id)
                        else Ruta.Historial.crear(idEquipo)       // sin pruebas todavía: historial
                    )
                },
                onVerHistorial = { idEquipo -> navController.navigate(Ruta.Historial.crear(idEquipo)) },
                onVolver = { navController.navigateUp() }
            )
        }
        // ---- Empresas en modo consulta: técnico ve todas, cliente solo la suya ----
        composable(Ruta.EmpresasConsulta.ruta) {
            val esCliente = usuarioActual?.rolId == Rol.ID_CLIENTE
            EmpresasConsultaScreen(
                titulo = stringResource(if (esCliente) R.string.menu_mi_empresa else R.string.menu_empresas),
                indicacion = stringResource(
                    if (esCliente) R.string.indicacion_mi_empresa else R.string.indicacion_empresas_consulta
                ),
                empresas = if (esCliente) empresas.filter { it.idEmpresa == usuarioActual?.idEmpresa } else empresas,
                onVolver = { navController.navigateUp() }
            )
        }


        // ---- Administrador (Tarea 3) ----
        composable(Ruta.InicioAdministrador.ruta) {
            InicioAdministradorScreen(
                usuario = usuarioActual,
                onIrUsuarios = { navController.navigate(Ruta.AdminUsuarios.ruta) },
                onIrEquipos = { navController.navigate(Ruta.AdminEquipos.ruta) },
                onIrEmpresas = { navController.navigate(Ruta.AdminEmpresas.ruta) },
                onEditarPerfil = { navController.navigate(Ruta.Perfil.ruta) },
                onCerrarSesion = {
                    protocolosViewModel.cerrarSesion()
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Ruta.AdminUsuarios.ruta) {
            AdminUsuariosScreen(
                usuarios = usuariosAdmin,
                onNuevoUsuario = {
                    adminUsuariosViewModel.abrirFormulario(0)             // 0 = formulario vacío
                    navController.navigate(Ruta.AdminUsuarioForm.ruta)
                },
                onEditarUsuario = { idUsuario ->
                    adminUsuariosViewModel.abrirFormulario(idUsuario)     // carga los datos del usuario
                    navController.navigate(Ruta.AdminUsuarioForm.ruta)
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.AdminUsuarioForm.ruta) {
            AdminUsuarioFormScreen(
                viewModel = adminUsuariosViewModel,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.AdminEmpresas.ruta) {
            AdminEmpresasScreen(
                empresas = empresasAdmin,
                onNuevaEmpresa = {
                    adminEmpresasViewModel.abrirFormulario(0)             // 0 = formulario vacío
                    navController.navigate(Ruta.AdminEmpresaForm.ruta)
                },
                onEditarEmpresa = { idEmpresa ->
                    adminEmpresasViewModel.abrirFormulario(idEmpresa)     // carga los datos de la empresa
                    navController.navigate(Ruta.AdminEmpresaForm.ruta)
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.AdminEmpresaForm.ruta) {
            AdminEmpresaFormScreen(
                viewModel = adminEmpresasViewModel,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.AdminEquipos.ruta) {
            AdminEquiposScreen(
                equipos = equiposAdmin,
                onNuevoEquipo = {
                    adminEquiposViewModel.abrirFormulario(0)              // 0 = formulario vacío
                    navController.navigate(Ruta.AdminEquipoForm.ruta)
                },
                onEditarEquipo = { idEquipo ->
                    adminEquiposViewModel.abrirFormulario(idEquipo)       // carga los datos del equipo
                    navController.navigate(Ruta.AdminEquipoForm.ruta)
                },
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.AdminEquipoForm.ruta) {
            AdminEquipoFormScreen(
                viewModel = adminEquiposViewModel,
                onVolver = { navController.navigateUp() }
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
                viewModel = perfilViewModel,
                onPerfilGuardado = { protocolosViewModel.refrescarUsuarioActual() },
                onVolver = { navController.navigateUp() }
            )
        }
    }
}


private fun rutaDeInicio(usuario: Usuario?): String = when {
    usuario == null -> Ruta.Login.ruta                       // sin sesión -> login
    usuario.rolId == Rol.ID_TECNICO -> Ruta.InicioTecnico.ruta
    usuario.rolId == Rol.ID_ADMINISTRADOR -> Ruta.InicioAdministrador.ruta
    else -> Ruta.InicioCliente.ruta
}