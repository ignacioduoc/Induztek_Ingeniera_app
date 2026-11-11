package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.components.OpcionMenu
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioAdministradorScreen(
    usuario: Usuario?,
    onIrUsuarios: () -> Unit,
    onIrEquipos: () -> Unit,
    onIrEmpresas: () -> Unit,
    onEditarPerfil: () -> Unit = {},
    onCerrarSesion: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val textoUsuarios = stringResource(R.string.menu_usuarios)
    val textoEquipos = stringResource(R.string.menu_equipos)
    val textoEmpresas = stringResource(R.string.menu_empresas)
    val textoPerfil = stringResource(R.string.menu_mi_perfil)

    // Cada gestión es una opción separada del menú deslizable
    val opciones = listOf(
        OpcionMenu(textoUsuarios, Icons.Default.Group, onIrUsuarios),
        OpcionMenu(textoEquipos, Icons.Default.Build, onIrEquipos),
        OpcionMenu(textoEmpresas, Icons.Default.Business, onIrEmpresas),
        OpcionMenu(textoPerfil, Icons.Default.Person, onEditarPerfil)
    )

    MenuSesion(drawerState = drawerState, onCerrarSesion = onCerrarSesion, usuario = usuario, opciones = opciones) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.titulo_inicio_administrador)) },
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
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.espacioPantalla),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.saludo, usuario?.nombre ?: ""),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.indicacion_administrador),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

                TarjetaGestion(
                    icono = Icons.Default.Person,
                    titulo = textoUsuarios,
                    descripcion = stringResource(R.string.descripcion_gestion_usuarios),
                    onClick = onIrUsuarios
                )
                TarjetaGestion(
                    icono = Icons.Default.Build,
                    titulo = textoEquipos,
                    descripcion = stringResource(R.string.descripcion_gestion_equipos),
                    onClick = onIrEquipos
                )
                TarjetaGestion(
                    icono = Icons.Default.Business,
                    titulo = textoEmpresas,
                    descripcion = stringResource(R.string.descripcion_gestion_empresas),
                    onClick = onIrEmpresas
                )
            }
        }
    }
}


@Composable
private fun TarjetaGestion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(Dimens.espacioMini)
            )
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InicioAdministradorPreview() {
    InduztekTheme {
        InicioAdministradorScreen(
            usuario = Usuario(
                nombre = "Admin Induztek",
                correo = "admin@induztek.cl",
                empresa = "Induztek Ingeniería SpA",
                rolId = Rol.ID_ADMINISTRADOR,
                rolNombre = "Administrador"
            ),
            onIrUsuarios = {}, onIrEquipos = {}, onIrEmpresas = {}, onCerrarSesion = {}
        )
    }
}