package com.example.induztek_ingeniera_app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioClienteScreen(
    usuario: Usuario?,
    protocolos: List<Protocolo>,
    onEditarPerfil: () -> Unit = {},
    onCerrarSesion: () -> Unit,
    onVerProtocolo: (Long) -> Unit
) {

    val bitmapFoto = remember(usuario?.fotoPerfil) {
        usuario?.fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

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
                                    text = usuario?.nombre?.take(1)?.uppercase() ?: "C",
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
                    text = stringResource(R.string.indicacion_protocolos),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = Dimens.espacioPantalla)
                )

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
                    items(protocolos, key = { it.id }) { protocolo ->
                        Card(onClick = { onVerProtocolo(protocolo.id) }) {
                            ListItem(
                                overlineContent = { Text(formatearFecha(protocolo.fechaHora)) },
                                headlineContent = { Text("${protocolo.codigoEquipo} · ${protocolo.tipoEquipo}") },
                                supportingContent = {
                                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                        Text(protocolo.instalacion)
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

private val protocolosPreview = listOf(
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
        resultado = Resultado.CON_OBSERVACION,
        observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
        estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
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
            onEditarPerfil = {},
            onCerrarSesion = {},
            onVerProtocolo = {}
        )
    }
}