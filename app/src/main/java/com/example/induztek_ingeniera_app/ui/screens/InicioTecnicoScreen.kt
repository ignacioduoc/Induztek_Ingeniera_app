package com.example.induztek_ingeniera_app.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.utils.buscarCoordenadasGratis
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTecnicoScreen(
    usuario: Usuario?,
    equipos: List<Equipo>,
    onEditarPerfil: () -> Unit = {},
    onCerrarSesion: () -> Unit,
    onNuevaPrueba: (Long) -> Unit = {},
    onVerHistorial: (Long) -> Unit = {},
    onVerMapa: (lat: Double, lng: Double, codigo: String, instalacion: String) -> Unit = { _, _, _, _ -> },
    onGuardarUbicacion: (idEquipo: Long, direccion: String, lat: Double, lng: Double) -> Unit = { _, _, _, _ -> }
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    val bitmapFoto = remember(usuario?.fotoPerfil) {
        usuario?.fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }


    var equipoAEditar by remember { mutableStateOf<Equipo?>(null) }
    var textoDireccionTemp by remember { mutableStateOf("") }
    var estaBuscando by remember { mutableStateOf(false) }


    equipoAEditar?.let { equipo ->
        AlertDialog(
            onDismissRequest = {
                if (!estaBuscando) equipoAEditar = null
            },
            title = { Text("Actualizar Dirección") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Equipo: ${equipo.codigo}", style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = textoDireccionTemp,
                        onValueChange = { textoDireccionTemp = it },
                        label = { Text("Dirección (Ej: Camino Los Maitenes 1200, Lampa)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !estaBuscando
                    )
                    if (estaBuscando) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !estaBuscando && textoDireccionTemp.isNotBlank(),
                    onClick = {
                        estaBuscando = true
                        buscarCoordenadasGratis(context, textoDireccionTemp) { geoPoint ->
                            estaBuscando = false
                            if (geoPoint != null) {
                                onGuardarUbicacion(
                                    equipo.id,
                                    textoDireccionTemp,
                                    geoPoint.latitude,
                                    geoPoint.longitude
                                )
                                Toast.makeText(context, "Ubicación actualizada correctamente", Toast.LENGTH_SHORT).show()
                                equipoAEditar = null
                            } else {
                                Toast.makeText(context, "No se encontraron coordenadas para esa dirección", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { equipoAEditar = null },
                    enabled = !estaBuscando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                // Encabezado interactivo: Avatar + Saludo + Botón Editar Perfil
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.espacioPantalla)
                        .clickable { onEditarPerfil() }
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.espacioMedio),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                    ) {
                        // Avatar circular con foto o inicial
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bitmapFoto != null) {
                                Image(
                                    bitmap = bitmapFoto.asImageBitmap(),
                                    contentDescription = "Foto de Perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = usuario?.nombre?.take(1)?.uppercase() ?: "T",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }


                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
                        ) {
                            Text(
                                text = stringResource(R.string.saludo, usuario?.nombre ?: ""),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.rol_empresa, usuario?.rolNombre ?: "", usuario?.empresa ?: ""),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }


                        IconButton(onClick = onEditarPerfil) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.indicacion_equipos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Dimens.espacioPantalla)
                )

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
                    items(equipos, key = { it.id }) { equipo ->
                        Card(onClick = { onNuevaPrueba(equipo.id) }) {
                            ListItem(
                                overlineContent = { Text(equipo.empresaCliente) },
                                headlineContent = { Text("${equipo.codigo} · ${equipo.tipo}") },
                                supportingContent = {
                                    Column {
                                        Text(equipo.instalacion)
                                        Text(
                                            text = if (equipo.direccion.isNotBlank()) "📍 ${equipo.direccion}" else "✏️ Agregar dirección",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .clickable {
                                                    textoDireccionTemp = equipo.direccion
                                                    equipoAEditar = equipo
                                                }
                                        )
                                    }
                                },
                                trailingContent = {
                                    Row {
                                        IconButton(onClick = {
                                            if (equipo.latitud != 0.0 && equipo.longitud != 0.0) {
                                                onVerMapa(equipo.latitud, equipo.longitud, equipo.codigo, equipo.instalacion)
                                            } else {
                                                Toast.makeText(context, "Ingresa primero una dirección válida", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_menu),
                                                contentDescription = "Ver Ubicación GPS",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { onVerHistorial(equipo.id) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_bar_chart),
                                                contentDescription = stringResource(R.string.cd_historial),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
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


private val equiposPreview = listOf(
    Equipo("TR-0098", "Transformador 300 kVA", "Planta Lampa · S/E-01", clienteId = 2, id = 2, empresaCliente = "Industrias Aurora S.A.", direccion = "Camino Los Maitenes 1200, Lampa", latitud = -33.2845, longitud = -70.8752),
    Equipo("TR-0142", "Transformador 500 kVA", "Planta Lampa · S/E-02", clienteId = 2, id = 1, empresaCliente = "Industrias Aurora S.A.", direccion = "Camino Los Maitenes 1200, Lampa", latitud = -33.2845, longitud = -70.8752),
    Equipo("TR-0215", "Transformador 1000 kVA", "Planta Quilicura · S/E-03", clienteId = 2, id = 3, empresaCliente = "Industrias Aurora S.A.", direccion = "Av. Américo Vespucio 1500, Quilicura", latitud = -33.3611, longitud = -70.7305),
    Equipo("TR-0330", "Transformador 750 kVA", "Planta Puerto Montt · S/E-01", clienteId = 3, id = 4, empresaCliente = "Envases del Sur S.A.", direccion = "Ruta 5 Sur Km 1020, Puerto Montt", latitud = -41.4693, longitud = -72.9424)
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
            onEditarPerfil = {},
            onCerrarSesion = {}
        )
    }
}