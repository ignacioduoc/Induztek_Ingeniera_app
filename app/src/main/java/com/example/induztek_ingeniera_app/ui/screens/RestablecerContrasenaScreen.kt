package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.RestablecerUiState
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.BotonSecundario
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.ExitoFondo
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel

// =====================================================================
//  RestablecerContrasenaScreen.kt  —  PANTALLA 7: RESTABLECER CONTRASEÑA
//  ---------------------------------------------------------------------
//  Se llega desde el login con "¿Olvidaste tu contraseña?".
//  Dos funciones, igual que LoginScreen y las pantallas del profe:
//   - RestablecerContrasenaScreen  = se conecta con el ViewModel: lee el
//                                    estado y le avisa lo que hace el usuario.
//   - RestablecerContrasenaContent = solo dibuja con lo que recibe. Como no
//                                    usa el ViewModel, se puede ver con @Preview.
// =====================================================================

@Composable
fun RestablecerContrasenaScreen(
    onVolver: () -> Unit,
    viewModel: RestablecerContrasenaViewModel
) {
    // Lee el StateFlow del ViewModel. Cada vez que cambia, la pantalla se redibuja.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RestablecerContrasenaContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onNuevaCambia = viewModel::onNuevaCambia,
        onConfirmarCambia = viewModel::onConfirmarCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onCambiar = viewModel::onCambiar,
        onVolver = onVolver
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestablecerContrasenaContent(
    uiState: RestablecerUiState,
    onCorreoCambia: (String) -> Unit,
    onNuevaCambia: (String) -> Unit,
    onConfirmarCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onCambiar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        // Barra superior con flecha para volver al login (igual que en Nuevo protocolo)
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_restablecer)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())   // se puede deslizar si aparece el teclado
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Text(
                text = stringResource(R.string.indicacion_restablecer),
                style = MaterialTheme.typography.bodyMedium
            )

            CampoTexto(
                valor = uiState.correo,
                onValorCambia = onCorreoCambia,
                etiqueta = stringResource(R.string.campo_correo),
                icono = R.drawable.ic_correo,
                error = uiState.errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            // Los dos campos usan el MISMO estado "contrasenaVisible":
            // al tocar cualquiera de los dos ojos se muestran (u ocultan) ambas.
            CampoContrasena(
                valor = uiState.nueva,
                onValorCambia = onNuevaCambia,
                etiqueta = stringResource(R.string.campo_nueva_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorNueva
            )
            CampoContrasena(
                valor = uiState.confirmar,
                onValorCambia = onConfirmarCambia,
                etiqueta = stringResource(R.string.campo_confirmar_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorConfirmar
            )

            BotonPrincipal(
                texto = stringResource(R.string.btn_cambiar_contrasena),
                onClick = onCambiar,
                icono = R.drawable.ic_candado,
                modifier = Modifier.fillMaxWidth()
            )

            // Solo aparece cuando la BD ya guardó la contraseña nueva
            if (uiState.cambioExitoso) {
                MensajeContrasenaCambiada()
                BotonSecundario(
                    texto = stringResource(R.string.btn_volver_login),
                    onClick = onVolver,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Tarjeta verde de confirmación, igual que el MensajeExito del formulario del profe
@Composable
private fun MensajeContrasenaCambiada() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExitoFondo, contentColor = Exito)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Exito,
                modifier = Modifier.size(Dimens.tamanoIcono)
            )
            Text(
                text = stringResource(R.string.contrasena_cambiada),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RestablecerVacioPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(),
            onCorreoCambia = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con errores")
@Composable
private fun RestablecerConErroresPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(
                correo = "tecnico@gmail.cl",
                nueva = "lampa26",
                confirmar = "lampa25",
                errorConfirmar = "Las contraseñas no coinciden"
            ),
            onCorreoCambia = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Contraseña cambiada")
@Composable
private fun RestablecerExitoPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(
                correo = "tecnico@gmail.cl",
                nueva = "lampa26",
                confirmar = "lampa26",
                cambioExitoso = true
            ),
            onCorreoCambia = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}