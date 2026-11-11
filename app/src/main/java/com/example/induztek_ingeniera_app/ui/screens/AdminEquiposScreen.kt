package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEquiposScreen(
    equipos: List<Equipo>,
    onNuevoEquipo: () -> Unit,
    onEditarEquipo: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // Lo que escribió el administrador en la búsqueda y lo elegido en los filtros
    var consulta by rememberSaveable { mutableStateOf("") }
    var empresaElegida by rememberSaveable { mutableStateOf<String?>(null) }
    var clienteElegido by rememberSaveable { mutableStateOf<String?>(null) }

    val empresasLista = equipos.map { it.empresaCliente }.filter { it.isNotBlank() }.distinct().sorted()
    // Si ya se eligió una empresa, solo se ofrecen los clientes de esa empresa
    val clientesLista = equipos
        .filter { empresaElegida == null || it.empresaCliente == empresaElegida }
        .map { it.nombreCliente }.filter { it.isNotBlank() }.distinct().sorted()

    val equiposFiltrados = equipos.filter { e ->
        (empresaElegida == null || e.empresaCliente == empresaElegida) &&
                (clienteElegido == null || e.nombreCliente == clienteElegido) &&
                (coincideBusqueda(e.codigo, consulta) ||
                        coincideBusqueda(e.tipo, consulta) ||
                        coincideBusqueda(e.instalacion, consulta) ||
                        coincideBusqueda(e.nombreCliente, consulta) ||
                        coincideBusqueda(e.empresaCliente, consulta))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_equipos)) },
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
            FloatingActionButton(
                onClick = onNuevoEquipo,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_nuevo_equipo))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            BarraBusquedaFiltro(
                consulta = consulta,
                onConsultaCambia = { consulta = it },
                placeholder = stringResource(R.string.buscar_equipos),
                grupos = listOf(
                    GrupoFiltro(
                        titulo = stringResource(R.string.filtro_empresa),
                        opciones = empresasLista,
                        seleccionada = empresaElegida,
                        onElegir = {
                            empresaElegida = it
                            clienteElegido = null          // al cambiar de empresa se vuelve a elegir el cliente
                        }
                    ),
                    GrupoFiltro(
                        titulo = stringResource(R.string.filtro_cliente),
                        opciones = clientesLista,
                        seleccionada = clienteElegido,
                        onElegir = { clienteElegido = it }
                    )
                ),
                modifier = Modifier.padding(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio, top = Dimens.espacioChico
                )
            )

            Text(
                text = stringResource(R.string.indicacion_equipos_admin),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
            )

            if (equipos.isEmpty() || equiposFiltrados.isEmpty()) {
                Text(
                    text = stringResource(
                        if (equipos.isEmpty()) R.string.lista_vacia_equipos else R.string.sin_resultados
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // el espacio de abajo evita que el botón "+" tape la última tarjeta
                contentPadding = PaddingValues(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio,
                    top = Dimens.espacioChico, bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(equiposFiltrados, key = { it.id }) { equipo ->
                    TarjetaEquipoAdmin(equipo = equipo, onClick = { onEditarEquipo(equipo.id) })
                }
            }
        }
    }
}


@Composable
private fun TarjetaEquipoAdmin(equipo: Equipo, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Text(
                text = "${equipo.codigo} · ${equipo.tipo}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = equipo.instalacion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (equipo.nombreCliente.isNotBlank()) {
                FilaDato(
                    icono = Icons.Default.Person,
                    etiqueta = stringResource(R.string.etiqueta_cliente_tarjeta),
                    valor = equipo.nombreCliente
                )
            }
            if (equipo.empresaCliente.isNotBlank()) {
                FilaDato(
                    icono = Icons.Default.Business,
                    etiqueta = stringResource(R.string.etiqueta_empresa_tarjeta),
                    valor = equipo.empresaCliente
                )
            }
            FilaDato(
                icono = Icons.Default.LocationOn,
                etiqueta = stringResource(R.string.etiqueta_direccion_tarjeta),
                valor = equipo.direccion.ifBlank { equipo.direccionEmpresa }
                    .ifBlank { stringResource(R.string.sin_direccion_registrada) }
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminEquiposPreview() {
    InduztekTheme {
        AdminEquiposScreen(
            equipos = listOf(
                Equipo(
                    id = 1, codigo = "TR-0142", tipo = "Transformador 500 kVA",
                    instalacion = "Planta Lampa · S/E-02", empresaCliente = "Industrias Aurora S.A.",
                    nombreCliente = "Pedro Salas", direccion = "Camino Los Maitenes 1200, Lampa"
                )
            ),
            onNuevoEquipo = {}, onEditarEquipo = {}, onVolver = {}
        )
    }
}