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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.utils.abrirEnMapas


/**
 * "Mis equipos" del cliente: solo los equipos que le pertenecen.
 * Tocar un equipo abre el detalle de su última prueba; el ícono del mapa muestra dónde está.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisEquiposScreen(
    equipos: List<Equipo>,
    protocolos: List<Protocolo> = emptyList(),   // para mostrar estado y técnico de la última prueba
    onVerUltimaPrueba: (Long) -> Unit,
    onVerHistorial: (Long) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var consulta by rememberSaveable { mutableStateOf("") }
    var instalacionElegida by rememberSaveable { mutableStateOf<String?>(null) }

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
                    // Última prueba del equipo (la más reciente) para mostrar su estado y técnico
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
                        onClick = { onVerUltimaPrueba(equipo.id) },     // tarjeta: detalle de su última prueba
                        onVerMapa = { abrirEnMapas(context, equipo.latitud, equipo.longitud, direccion) },
                        onVerHistorial = { onVerHistorial(equipo.id) }  // estadísticas: historial
                    )
                }
            }
        }
    }
}
/**
 * Tarjeta de "Mis equipos", reutilizada en "Mis protocolos" y en el "Historial del equipo".
 * Los datos opcionales (fecha, resultado, estado, técnico, empresa) solo se muestran si existen.
 */
@Composable
fun TarjetaMiEquipo(
    titulo: String,                    // "TR-0142 · Transformador 500 kVA"
    instalacion: String,
    direccion: String,
    onVerMapa: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,          // null = la tarjeta no es clickeable (ej.: dentro del historial)
    onVerHistorial: (() -> Unit)? = null,   // null = sin ícono de estadísticas
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
                // Ubicación del equipo en la app de mapas
                IconButton(onClick = onVerMapa) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.cd_ver_mapa),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Estadísticas: historial de pruebas del equipo (solo si se entrega la acción)
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

            // Resultado y estado de la última prueba (Aprobado / Formalizado, etc.)
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
            // "Revisado por: técnico"
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