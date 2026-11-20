package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
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
    equipos: List<Equipo> = emptyList(),
    onIrMisEquipos: () -> Unit = {},
    onIrMiEmpresa: () -> Unit = {},
    onEditarPerfil: () -> Unit = {},
    onCerrarSesion: () -> Unit,
    onVerProtocolo: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit = {},
    onSolicitarEquipo: ((codigo: String, tipo: String, instalacion: String, direccion: String, onResultado: (Boolean) -> Unit) -> Unit)? = null
) {

    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var consulta by rememberSaveable { mutableStateOf("") }
    var resultadoElegido by rememberSaveable { mutableStateOf<String?>(null) }
    var estadoElegido by rememberSaveable { mutableStateOf<String?>(null) }
    var equipoElegido by rememberSaveable { mutableStateOf<String?>(null) }

    var mostrarDialogoSolicitud by rememberSaveable { mutableStateOf(false) }
    var codigo by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("") }
    var instalacion by rememberSaveable { mutableStateOf("") }
    var direccionNueva by rememberSaveable { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf<String?>(null) }

    // Equipos del cliente que aún están en proceso de solicitud (sin protocolo registrado todavía)
    val equiposConProtocoloIds = protocolos.map { it.equipoId }.toSet()
    val solicitudesEnCurso = equipos.filter { it.id !in equiposConProtocoloIds }

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
                                contentDescription = "Menú"
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = {
                        codigo = ""
                        tipo = ""
                        instalacion = ""
                        direccionNueva = ""
                        errorFormulario = null
                        mostrarDialogoSolicitud = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Ingresar Solicitud") }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(Dimens.espacioMedio),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                item {
                    TarjetaBienvenida(usuario = usuario)
                }

                // Sección de seguimiento en tiempo real de solicitudes recién ingresadas
                if (solicitudesEnCurso.isNotEmpty()) {
                    item {
                        Text(
                            text = "Solicitudes en curso (${solicitudesEnCurso.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = Dimens.espacioChico)
                        )
                    }
                    items(solicitudesEnCurso, key = { "sol_${it.id}" }) { equipo ->
                        TarjetaSolicitudEstado(equipo = equipo)
                    }
                }

                item {
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
                        )
                    )
                }

                item {
                    Text(
                        text = stringResource(R.string.indicacion_protocolos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (protocolos.isEmpty() || protocolosFiltrados.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(
                                if (protocolos.isEmpty()) R.string.lista_vacia_protocolos else R.string.sin_resultados
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = Dimens.espacioChico)
                        )
                    }
                }

                items(protocolosFiltrados, key = { it.id }) { protocolo ->
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

    if (mostrarDialogoSolicitud) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSolicitud = false },
            title = { Text("Nueva solicitud de inspección") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(
                        valor = codigo,
                        onValorCambia = { codigo = it; errorFormulario = null },
                        etiqueta = "Código del equipo (ej. TR-0450)"
                    )
                    CampoTexto(
                        valor = tipo,
                        onValorCambia = { tipo = it; errorFormulario = null },
                        etiqueta = "Tipo de equipo (ej. Transformador 500 kVA)"
                    )
                    CampoTexto(
                        valor = instalacion,
                        onValorCambia = { instalacion = it; errorFormulario = null },
                        etiqueta = "Instalación (ej. Planta Sur · S/E-01)"
                    )
                    CampoTexto(
                        valor = direccionNueva,
                        onValorCambia = { direccionNueva = it; errorFormulario = null },
                        etiqueta = "Dirección"
                    )
                    errorFormulario?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (codigo.isBlank() || tipo.isBlank() || instalacion.isBlank()) {
                        errorFormulario = "Completa código, tipo e instalación"
                        return@TextButton
                    }
                    onSolicitarEquipo?.invoke(codigo, tipo, instalacion, direccionNueva) { exito ->
                        if (exito) {
                            mostrarDialogoSolicitud = false
                        } else {
                            errorFormulario = "Ya existe un equipo con ese código"
                        }
                    }
                }) {
                    Text("Enviar solicitud")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoSolicitud = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun TarjetaSolicitudEstado(equipo: Equipo) {
    val tieneTecnico = equipo.nombreTecnico.isNotBlank()
    val estadoTexto = if (tieneTecnico) {
        "Técnico asignado: ${equipo.nombreTecnico} (en espera de inspección)"
    } else {
        "Pendiente de asignar técnico por el Supervisor"
    }
    val colorFondo = if (tieneTecnico) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val colorTexto = if (tieneTecnico) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${equipo.codigo} · ${equipo.tipo}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = equipo.instalacion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = colorFondo,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = estadoTexto,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorTexto,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
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