package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import kotlinx.coroutines.launch

// =====================================================================
//  InicioTecnicoScreen.kt  —  PANTALLA 2: INICIO DEL TÉCNICO
//  ---------------------------------------------------------------------
//  Barra superior con el menú ☰, el saludo y la LISTA DE EQUIPOS.
//  La lista se arma igual que en la UsuariosScreen del profe:
//     LazyColumn -> lista que solo dibuja las filas que se ven en pantalla
//     items(...) -> una fila (Card + ListItem) por cada equipo
//  Tocar la tarjeta = nueva prueba de ese equipo; tocar el gráfico = historial.
//  Recibe los datos y los eventos por parámetro (no conoce el ViewModel),
//  por eso se puede ver con @Preview.
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTecnicoScreen(
    usuario: Usuario?,
    equipos: List<Equipo>,
    onCerrarSesion: () -> Unit,
    // Con "= {}" funcionan aunque todavía no estén conectados (Pasos 11 y 14)
    onNuevaPrueba: (Long) -> Unit = {},
    onVerHistorial: (Long) -> Unit = {}
) {
    // Estado del menú lateral (abierto / cerrado) y el "scope" para abrirlo
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    MenuSesion(drawerState = drawerState, onCerrarSesion = onCerrarSesion) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.titulo_inicio_tecnico)) },
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
                        text = stringResource(R.string.indicacion_equipos),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Si la lista viene vacía se avisa (igual que en la lista del profe)
                if (equipos.isEmpty()) {
                    Text(
                        text = stringResource(R.string.lista_vacia_equipos),
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
                    items(equipos, key = { it.id }) { equipo ->
                        Card(onClick = { onNuevaPrueba(equipo.id) }) {
                            ListItem(
                                overlineContent = { Text(equipo.empresaCliente) },
                                headlineContent = { Text("${equipo.codigo} · ${equipo.tipo}") },
                                supportingContent = { Text(equipo.instalacion) },
                                trailingContent = {
                                    IconButton(onClick = { onVerHistorial(equipo.id) }) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_bar_chart),
                                            contentDescription = stringResource(R.string.cd_historial),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
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

// Datos de ejemplo para la vista previa (los mismos que precarga la BD)
private val equiposPreview = listOf(
    Equipo("TR-0098", "Transformador 300 kVA", "Planta Lampa · S/E-01", clienteId = 2, id = 2, empresaCliente = "Industrias Aurora S.A."),
    Equipo("TR-0142", "Transformador 500 kVA", "Planta Lampa · S/E-02", clienteId = 2, id = 1, empresaCliente = "Industrias Aurora S.A."),
    Equipo("TR-0215", "Transformador 1000 kVA", "Planta Quilicura · S/E-03", clienteId = 2, id = 3, empresaCliente = "Industrias Aurora S.A."),
    Equipo("TR-0330", "Transformador 750 kVA", "Planta Puerto Montt · S/E-01", clienteId = 3, id = 4, empresaCliente = "Envases del Sur S.A.")
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioTecnicoPreview() {
    InduztekTheme {
        InicioTecnicoScreen(
            usuario = Usuario(
                nombre = "Matías Rojas",
                correo = "tecnico@demo.cl",
                empresa = "Induztek Ingeniería SpA",
                rolId = Rol.ID_TECNICO,
                rolNombre = "Técnico"
            ),
            equipos = equiposPreview,
            onCerrarSesion = {}
        )
    }
}