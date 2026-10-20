package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import kotlinx.coroutines.launch

// =====================================================================
//  InicioClienteScreen.kt  —  PANTALLA 4: INICIO DEL CLIENTE
//  ---------------------------------------------------------------------
//  Barra superior con el menú ☰, el saludo y la LISTA DE SUS PROTOCOLOS
//  (los de sus equipos, del más nuevo al más antiguo).
//  La lista se arma igual que en la UsuariosScreen del profe:
//     LazyColumn -> lista que solo dibuja las filas que se ven en pantalla
//     items(...) -> una fila (Card + ListItem) por cada protocolo
//  Cada fila muestra dos chips de color: el resultado y el estado.
//  Tocar la tarjeta = abrir el detalle de ese protocolo.
//  Recibe los datos y los eventos por parámetro (no conoce el ViewModel),
//  por eso se puede ver con @Preview.
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioClienteScreen(
    usuario: Usuario?,
    protocolos: List<Protocolo>,
    onCerrarSesion: () -> Unit,
    onVerProtocolo: (Long) -> Unit
) {
    // Estado del menú lateral (abierto / cerrado) y el "scope" para abrirlo
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    MenuSesion(drawerState = drawerState, onCerrarSesion = onCerrarSesion) {
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
                // Encabezado: saludo, rol y empresa, y qué hacer en esta pantalla
                Column(
                    modifier = Modifier.padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
                ) {
                    // usuario?.nombre ?: "" -> si usuario es null (justo al cerrar sesión) queda vacío
                    Text(
                        text = stringResource(R.string.saludo, usuario?.nombre ?: ""),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.rol_empresa, usuario?.rolNombre ?: "", usuario?.empresa ?: ""),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.indicacion_protocolos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Si la lista viene vacía se avisa (igual que en la lista del profe)
                if (protocolos.isEmpty()) {
                    Text(
                        text = stringResource(R.string.lista_vacia_protocolos),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(Dimens.espacioMedio)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    // key = id: Compose reconoce cada fila aunque la lista cambie
                    items(protocolos, key = { it.id }) { protocolo ->
                        Card(onClick = { onVerProtocolo(protocolo.id) }) {
                            ListItem(
                                overlineContent = { Text(formatearFecha(protocolo.fechaHora)) },
                                // codigoEquipo y tipoEquipo llegan por el JOIN con la tabla equipos
                                headlineContent = { Text("${protocolo.codigoEquipo} · ${protocolo.tipoEquipo}") },
                                supportingContent = {
                                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                        Text(protocolo.instalacion)
                                        // Chips: resultado (verde / ámbar / rojo) y estado (azul / verde)
                                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                            EtiquetaEstado(texto = protocolo.resultado.etiqueta, color = colorDe(protocolo.resultado))
                                            EtiquetaEstado(texto = protocolo.estado.etiqueta, color = colorDe(protocolo.estado))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Datos de ejemplo para la vista previa: uno recién registrado en terreno
// y dos de la BD (ya formalizados)
private val protocolosPreview = listOf(
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
        resultado = Resultado.CON_OBSERVACION,
        observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
        estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
    ),
    Protocolo(
        equipoId = 2, tecnicoId = 1, fechaHora = 1776367200000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 11.2, atTierra = 8.7, btTierra = 7.9, indicePolarizacion = 2.6,
        resultado = Resultado.APROBADO, observaciones = "",
        estado = EstadoProtocolo.FORMALIZADO, id = 4,
        codigoEquipo = "TR-0098", tipoEquipo = "Transformador 300 kVA",
        instalacion = "Planta Lampa · S/E-01", nombreTecnico = "Matías Rojas"
    ),
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1776174300000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 13.1, atTierra = 9.4, btTierra = 7.8, indicePolarizacion = 2.4,
        resultado = Resultado.APROBADO, observaciones = "",
        estado = EstadoProtocolo.FORMALIZADO, id = 3,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
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
            onCerrarSesion = {},
            onVerProtocolo = {}
        )
    }
}