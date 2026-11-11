package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.theme.Acento
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.theme.Secundario


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuariosScreen(
    usuarios: List<Usuario>,
    onNuevoUsuario: () -> Unit,
    onEditarUsuario: (Long) -> Unit,
    onVolver: () -> Unit
) {
    // Lo que escribió el administrador en la búsqueda y el rol elegido en el filtro
    var consulta by rememberSaveable { mutableStateOf("") }
    var rolElegido by rememberSaveable { mutableStateOf<String?>(null) }

    val roles = usuarios.map { it.rolNombre }.filter { it.isNotBlank() }.distinct().sorted()
    val usuariosFiltrados = usuarios.filter { u ->
        (rolElegido == null || u.rolNombre == rolElegido) &&
                (coincideBusqueda(u.nombreCompleto, consulta) ||
                        coincideBusqueda(u.correo, consulta) ||
                        coincideBusqueda(u.rut, consulta) ||
                        coincideBusqueda(u.empresa, consulta))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_usuarios)) },
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
                onClick = onNuevoUsuario,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_nuevo_usuario))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            BarraBusquedaFiltro(
                consulta = consulta,
                onConsultaCambia = { consulta = it },
                placeholder = stringResource(R.string.buscar_usuarios),
                grupos = listOf(
                    GrupoFiltro(
                        titulo = stringResource(R.string.filtro_rol),
                        opciones = roles,
                        seleccionada = rolElegido,
                        onElegir = { rolElegido = it }
                    )
                ),
                modifier = Modifier.padding(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio, top = Dimens.espacioChico
                )
            )

            Text(
                text = stringResource(R.string.indicacion_usuarios),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
            )

            if (usuarios.isEmpty() || usuariosFiltrados.isEmpty()) {
                Text(
                    text = stringResource(
                        if (usuarios.isEmpty()) R.string.lista_vacia_usuarios else R.string.sin_resultados
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
                items(usuariosFiltrados, key = { it.id }) { usuario ->
                    TarjetaUsuario(usuario = usuario, onClick = { onEditarUsuario(usuario.id) })
                }
            }
        }
    }
}


@Composable
private fun TarjetaUsuario(usuario: Usuario, onClick: () -> Unit) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                // Círculo con la inicial del nombre
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombreCompleto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                EtiquetaEstado(texto = usuario.rolNombre, color = colorDeRol(usuario.rolId))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            if (usuario.rut.isNotBlank()) {
                FilaDato(
                    icono = Icons.Default.Badge,
                    etiqueta = stringResource(R.string.etiqueta_rut_tarjeta),
                    valor = usuario.rut
                )
            }
            FilaDato(
                icono = Icons.Default.Business,
                etiqueta = stringResource(R.string.etiqueta_empresa_tarjeta),
                valor = usuario.empresa.ifBlank { stringResource(R.string.sin_empresa) }
            )
        }
    }
}


private fun colorDeRol(rolId: Long): Color = when (rolId) {
    Rol.ID_ADMINISTRADOR -> Acento      // ámbar
    Rol.ID_TECNICO -> Secundario        // azul
    else -> Exito                       // verde (cliente)
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AdminUsuariosPreview() {
    InduztekTheme {
        AdminUsuariosScreen(
            usuarios = listOf(
                Usuario(
                    id = 1, nombre = "Matías", apellido = "Rojas", correo = "tecnico@gmail.com",
                    rut = "12345678-5", empresa = "Induztek Ingeniería SpA",
                    rolId = Rol.ID_TECNICO, rolNombre = "Técnico"
                ),
                Usuario(
                    id = 2, nombre = "Pedro", apellido = "Salas", correo = "cliente@gmail.com",
                    rut = "11222333-9", empresa = "Industrias Aurora S.A.",
                    rolId = Rol.ID_CLIENTE, rolNombre = "Cliente"
                )
            ),
            onNuevoUsuario = {}, onEditarUsuario = {}, onVolver = {}
        )
    }
}