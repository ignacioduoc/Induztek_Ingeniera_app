package com.example.induztek_ingeniera_app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import kotlinx.coroutines.launch


/** Una opción del menú lateral (ej: Usuarios, Equipos, Mi perfil). */
data class OpcionMenu(
    val etiqueta: String,
    val icono: ImageVector,
    val onClick: () -> Unit,
    val seleccionada: Boolean = false       // true = la pantalla en la que estás (se pinta resaltada)
)


/**
 * Menú lateral de los tres roles: encabezado con la foto y los datos del usuario,
 * las opciones de su rol y "Cerrar sesión" abajo.
 */
@Composable
fun MenuSesion(
    drawerState: DrawerState,
    onCerrarSesion: () -> Unit,
    usuario: Usuario? = null,                      // para el encabezado (foto, nombre, rol y empresa)
    opciones: List<OpcionMenu> = emptyList(),
    contenido: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_titulo)) },
            text = { Text(stringResource(R.string.dialogo_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()
                }) { Text(stringResource(R.string.menu_cerrar_sesion)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // windowInsets en 0: el encabezado verde llega hasta arriba, detrás de la barra de estado
            ModalDrawerSheet(windowInsets = WindowInsets(0, 0, 0, 0)) {
                Column(modifier = Modifier.fillMaxHeight()) {

                    EncabezadoMenu(usuario = usuario)

                    // Opciones del rol
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = Dimens.espacioChico)
                    ) {
                        opciones.forEach { opcion ->
                            NavigationDrawerItem(
                                icon = { Icon(opcion.icono, contentDescription = null) },
                                label = { Text(opcion.etiqueta) },
                                selected = opcion.seleccionada,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    opcion.onClick()
                                },
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                            )
                        }
                    }

                    // Cerrar sesión, siempre al final
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationDrawerItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                        label = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            mostrarDialogo = true
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedIconColor = MaterialTheme.colorScheme.error,
                            unselectedTextColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                            .padding(vertical = Dimens.espacioChico)
                            .navigationBarsPadding()
                    )
                }
            }
        }
    ) {
        contenido()
    }
}


/** Parte de arriba del menú: foto (o inicial), nombre, correo, rol y empresa. */
@Composable
private fun EncabezadoMenu(usuario: Usuario?) {
    val bitmapFoto = remember(usuario?.fotoPerfil) {
        usuario?.fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(Dimens.espacioMedio),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        if (usuario == null) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            return@Column
        }

        // Avatar
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (bitmapFoto != null) {
                Image(
                    bitmap = bitmapFoto.asImageBitmap(),
                    contentDescription = stringResource(R.string.cd_foto_perfil),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = usuario.nombre.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimens.espacioMini))

        Text(
            text = usuario.nombreCompleto,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = usuario.correo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = usuario.rolNombre,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
                )
            }
            if (usuario.empresa.isNotBlank()) {
                Text(
                    text = usuario.empresa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MenuSesionPreview() {
    InduztekTheme {
        // DrawerValue.Open: la vista previa muestra el menú abierto
        MenuSesion(
            drawerState = rememberDrawerState(DrawerValue.Open),
            onCerrarSesion = {},
            usuario = Usuario(
                nombre = "Matías", apellido = "Rojas", correo = "tecnico@gmail.com",
                empresa = "Induztek Ingeniería SpA", rolId = Rol.ID_TECNICO, rolNombre = "Técnico"
            ),
            opciones = listOf(
                OpcionMenu("Equipos", Icons.Default.Build, {}, seleccionada = true),
                OpcionMenu("Empresas", Icons.Default.Business, {}),
                OpcionMenu("Mi perfil", Icons.Default.Person, {})
            )
        ) {
            Text("Pantalla de inicio")
        }
    }
}