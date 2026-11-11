package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.components.OpcionMenu
import com.example.induztek_ingeniera_app.ui.components.TarjetaBienvenida
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.utils.abrirEnMapas
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioClienteScreen(
    usuario: Usuario?,
    protocolos: List<Protocolo>,
    onIrMisEquipos: () -> Unit = {},
    onIrMiEmpresa: () -> Unit = {},
    onEditarPerfil: () -> Unit = {},
    onCerrarSesion: () -> Unit,
    onVerProtocolo: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit = {}
) {

    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Búsqueda y filtros de la lista de protocolos
    var consulta by rememberSaveable { mutableStateOf("") }
    var resultadoElegido by rememberSaveable { mutableStateOf<String?>(null) }
    var estadoElegido by rememberSaveable { mutableStateOf<String?>(null) }
    var equipoElegido by rememberSaveable { mutableStateOf<String?>(null) }

    val equiposLista = protocolos.map { it.codigoEquipo }.filter { it.isNotBlank() }.distinct().sorted()

    val protocolosFiltrados = protocolos.filter { p ->
        (resultadoElegido == null || p.resultado.etiqueta == resultadoElegido) &&
                (estadoElegido == null || p.estado.etiqueta == estadoElegido) &&
                (equipoElegido == null || p.codigoEquipo == equipoElegido) &&
                (coincideBusqueda(p.codigoEquipo, consulta) ||
                        coincideBusqueda(p.tipoEquipo, consulta) ||
                        coincideBusqueda(p.instalacion, consulta) ||
                        coincideBusqueda(p.nombreTecnico, consulta) ||
                        coincideBusqueda(p.tipoPrueba, consulta))
    }

    // Opciones del menú lateral del cliente ("Mis protocolos" es esta misma pantalla)
    val opcionesMenu = listOf(
        OpcionMenu(stringResource(R.string.titulo_inicio_cliente), Icons.Default.Description, {}, seleccionada = true),
        OpcionMenu(stringResource(R.string.menu_mis_equipos), Icons.Default.Build, onIrMisEquipos),
        OpcionMenu(stringResource(R.string.menu_mi_empresa), Icons.Default.Business, onIrMiEmpresa),
        OpcionMenu(stringResource(R.string.menu_mi_perfil), Icons.Default.Person, onEditarPerfil)
    )

    MenuSesion(
        drawerState = drawerState,
        onCerrarSesion = onCerrarSesion,
        usuario = usuario,
        opciones = opcionesMenu
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.titulo_inicio_cliente)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_menu),
                                contentDescription = stringResource(R.string.cd_menu)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                TarjetaBienvenida(
                    usuario = usuario,
                    modifier = Modifier.padding(Dimens.espacioPantalla)
                )

                BarraBusquedaFiltro(
                    consulta = consulta,
                    onConsultaCambia = { consulta = it },
                    placeholder = stringResource(R.string.buscar_protocolos),
                    grupos = listOf(
                        GrupoFiltro(
                            titulo = stringResource(R.string.filtro_resultado),
                            opciones = Resultado.entries.map { it.etiqueta },
                            seleccionada = resultadoElegido,
                            onElegir = { resultadoElegido = it }
                        ),
                        GrupoFiltro(
                            titulo = stringResource(R.string.filtro_estado),
                            opciones = EstadoProtocolo.entries.map { it.etiqueta },
                            seleccionada = estadoElegido,
                            onElegir = { estadoElegido = it }
                        ),
                        GrupoFiltro(
                            titulo = stringResource(R.string.filtro_equipo),
                            opciones = equiposLista,
                            seleccionada = equipoElegido,
                            onElegir = { equipoElegido = it }
                        )
                    ),
                    modifier = Modifier.padding(
                        start = Dimens.espacioPantalla, end = Dimens.espacioPantalla, bottom = Dimens.espacioChico
                    )
                )

                Text(
                    text = stringResource(R.string.indicacion_protocolos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Dimens.espacioPantalla)
                )

                if (protocolos.isEmpty() || protocolosFiltrados.isEmpty()) {
                    Text(
                        text = stringResource(
                            if (protocolos.isEmpty()) R.string.lista_vacia_protocolos else R.string.sin_resultados
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.espacioMedio)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    items(protocolosFiltrados, key = { it.id }) { protocolo ->
                        // Misma tarjeta que "Mis equipos"
                        TarjetaMiEquipo(
                            titulo = "${protocolo.codigoEquipo} · ${protocolo.tipoEquipo}",
                            instalacion = protocolo.instalacion,
                            direccion = protocolo.direccionEquipo,
                            fecha = formatearFecha(protocolo.fechaHora),
                            resultado = protocolo.resultado,
                            estado = protocolo.estado,
                            empresa = usuario?.empresa.orEmpty(),
                            tecnico = protocolo.nombreTecnico,
                            onClick = { onVerProtocolo(protocolo.id) },
                            onVerMapa = { abrirEnMapas(context, 0.0, 0.0, protocolo.direccionEquipo) },
                            onVerHistorial = { onVerHistorial(protocolo.equipoId) }
                        )
                    }
                }
            }
        }
    }
}

private val protocolosPreview = listOf(
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
        resultado = Resultado.CON_OBSERVACION,
        observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
        estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", direccionEquipo = "Camino Los Maitenes 1200, Lampa",
        nombreTecnico = "Matías Rojas"
    )
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioClientePreview() {
    InduztekTheme {
        InicioClienteScreen(
            usuario = Usuario(
                nombre = "Pedro Salas",
                correo = "cliente@demo.cl",
                empresa = "Industrias Aurora S.A.",
                rolId = Rol.ID_CLIENTE,
                rolNombre = "Cliente"
            ),
            protocolos = protocolosPreview,
            onEditarPerfil = {},
            onCerrarSesion = {},
            onVerProtocolo = {}
        )
    }
}