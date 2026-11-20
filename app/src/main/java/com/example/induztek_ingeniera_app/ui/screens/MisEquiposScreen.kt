package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.utils.abrirEnMapas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEquiposScreen(
    equipos: List<Equipo>,
    protocolos: List<Protocolo> = emptyList(),
    onVerUltimaPrueba: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit,
    onSolicitarEquipo: ((codigo: String, tipo: String, instalacion: String, direccion: String, onResultado: (Boolean) -> Unit) -> Unit)? = null,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var consulta by rememberSaveable { mutableStateOf("") }
    var instalacionElegida by rememberSaveable { mutableStateOf<String?>(null) }

    var mostrarDialogoSolicitud by rememberSaveable { mutableStateOf(false) }
    var codigo by rememberSaveable { mutableStateOf("") }
    var tipo by rememberSaveable { mutableStateOf("") }
    var instalacion by rememberSaveable { mutableStateOf("") }
    var direccionNueva by rememberSaveable { mutableStateOf("") }
    var errorFormulario by remember { mutableStateOf<String?>(null) }

    val instalaciones = equipos.map { it.instalacion }.filter { it.isNotBlank() }.distinct().sorted()
    val equiposFiltrados = equipos.filter { e ->
        (instalacionElegida == null || e.instalacion == instalacionElegida) &&
                (coincideBusqueda(e.codigo, consulta) ||
                        coincideBusqueda(e.tipo, consulta) ||
                        coincideBusqueda(e.instalacion, consulta) ||
                        coincideBusqueda(e.direccion, consulta))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_mis_equipos)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = stringResource(R.string.cd_volver)
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
            if (onSolicitarEquipo != null) {
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
                    text = { Text("Solicitar equipo") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            BarraBusquedaFiltro(
                consulta = consulta,
                onConsultaCambia = { consulta = it },
                placeholder = stringResource(R.string.buscar_mis_equipos),
                grupos = listOf(
                    GrupoFiltro(
                        titulo = stringResource(R.string.filtro_instalacion),
                        opciones = instalaciones,
                        seleccionada = instalacionElegida,
                        onElegir = { instalacionElegida = it }
                    )
                ),
                modifier = Modifier.padding(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio, top = Dimens.espacioChico
                )
            )

            Text(
                text = stringResource(R.string.indicacion_mis_equipos),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
            )

            if (equipos.isEmpty() || equiposFiltrados.isEmpty()) {
                Text(
                    text = stringResource(
                        if (equipos.isEmpty()) R.string.lista_vacia_mis_equipos else R.string.sin_resultados
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio,
                    top = Dimens.espacioChico, bottom = Dimens.espacioMedio
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(equiposFiltrados, key = { it.id }) { equipo ->
                    val ultima = protocolos.filter { it.equipoId == equipo.id }.maxByOrNull { it.fechaHora }
                    val direccion = equipo.direccion.ifBlank { equipo.direccionEmpresa }
                    TarjetaMiEquipo(
                        titulo = "${equipo.codigo} · ${equipo.tipo}",
                        instalacion = equipo.instalacion,
                        direccion = direccion,
                        fecha = ultima?.let { formatearFecha(it.fechaHora) },
                        resultado = ultima?.resultado,
                        estado = ultima?.estado,
                        empresa = equipo.empresaCliente,
                        tecnico = ultima?.nombreTecnico.orEmpty(),
                        onClick = { onVerUltimaPrueba(equipo.id) },
                        onVerMapa = { abrirEnMapas(context, equipo.latitud, equipo.longitud, direccion) },
                        onVerHistorial = { onVerHistorial(equipo.id) }
                    )
                }
            }
        }
    }

    if (mostrarDialogoSolicitud && onSolicitarEquipo != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoSolicitud = false },
            title = { Text("Solicitar inspección de equipo") },
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
                        etiqueta = "Tipo (ej. Transformador 500 kVA)"
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
                    onSolicitarEquipo(codigo, tipo, instalacion, direccionNueva) { exito ->
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
fun TarjetaMiEquipo(
    titulo: String,
    instalacion: String,
    direccion: String,
    onVerMapa: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onVerHistorial: (() -> Unit)? = null,
    fecha: String? = null,
    resultado: Resultado? = null,
    estado: EstadoProtocolo? = null,
    empresa: String = "",
    tecnico: String = ""
) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 2.dp)

    val contenido: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    if (fecha != null) {
                        Text(
                            text = fecha,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = instalacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onVerMapa) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cd_ver_mapa),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (onVerHistorial != null) {
                    IconButton(onClick = onVerHistorial) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bar_chart),
                            contentDescription = stringResource(R.string.cd_historial),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (resultado != null && estado != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    EtiquetaEstado(texto = resultado.etiqueta, color = colorDe(resultado))
                    EtiquetaEstado(texto = estado.etiqueta, color = colorDe(estado))
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (empresa.isNotBlank()) {
                FilaDato(
                    icono = Icons.Default.Business,
                    etiqueta = stringResource(R.string.etiqueta_empresa_tarjeta),
                    valor = empresa
                )
            }
            if (tecnico.isNotBlank()) {
                FilaDato(
                    icono = Icons.Default.Build,
                    etiqueta = stringResource(R.string.etiqueta_tecnico_tarjeta),
                    valor = tecnico
                )
            }
            FilaDato(
                icono = Icons.Default.LocationOn,
                etiqueta = stringResource(R.string.etiqueta_direccion_tarjeta),
                valor = direccion.ifBlank { stringResource(R.string.sin_direccion_registrada) }
            )
        }
    }

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier.fillMaxWidth(), colors = colores, elevation = elevacion, content = contenido)
    } else {
        Card(modifier = modifier.fillMaxWidth(), colors = colores, elevation = elevacion, content = contenido)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MisEquiposPreview() {
    InduztekTheme {
        MisEquiposScreen(
            equipos = listOf(
                Equipo(
                    id = 1, codigo = "TR-0142", tipo = "Transformador 500 kVA",
                    instalacion = "Planta Lampa · S/E-02", empresaCliente = "Industrias Aurora S.A.",
                    direccion = "Camino Los Maitenes 1200, Lampa", latitud = -33.28, longitud = -70.87
                )
            ),
            onVerUltimaPrueba = {}, onVerHistorial = {}, onVolver = {}
        )
    }
}