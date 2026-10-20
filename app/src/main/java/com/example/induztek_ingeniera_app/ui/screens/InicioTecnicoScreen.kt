package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.MenuSesion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import kotlinx.coroutines.launch

// =====================================================================
//  InicioTecnicoScreen.kt  —  PANTALLA 2: INICIO DEL TÉCNICO
//  ---------------------------------------------------------------------
//  Barra superior verde con el menú ☰ (para cerrar sesión) y el saludo.
//  En el Paso 10 se agrega la lista de equipos.
//  Recibe los datos y los eventos por parámetro (no conoce el ViewModel),
//  por eso se puede ver con @Preview.
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTecnicoScreen(
    usuario: Usuario?,
    onCerrarSesion: () -> Unit
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
                    .padding(Dimens.espacioPantalla),
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
            }
        }
    }
}

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
            onCerrarSesion = {}
        )
    }
}