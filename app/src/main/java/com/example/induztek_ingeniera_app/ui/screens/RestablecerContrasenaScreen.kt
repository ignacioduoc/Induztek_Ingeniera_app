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
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.ExitoFondo
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel


@Composable
fun RestablecerContrasenaScreen(
    onVolver: () -> Unit,
    viewModel: RestablecerContrasenaViewModel
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RestablecerContrasenaContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContinuar = viewModel::onContinuar,
        onRespuestaCambia = viewModel::onRespuestaCambia,
        onVerificarRespuesta = viewModel::onVerificarRespuesta,
        onVolverAlCorreo = viewModel::onVolverAlCorreo,
        onNuevaCambia = viewModel::onNuevaCambia,
        onConfirmarCambia = viewModel::onConfirmarCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onCambiar = viewModel::onCambiar,
        onVolver = onVolver
    )
}

// Tres pasos en la misma pantalla: 1) correo, 2) pregunta de seguridad, 3) contraseña nueva
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestablecerContrasenaContent(
    uiState: RestablecerUiState,
    onCorreoCambia: (String) -> Unit,
    onContinuar: () -> Unit,
    onRespuestaCambia: (String) -> Unit,
    onVerificarRespuesta: () -> Unit,
    onVolverAlCorreo: () -> Unit,
    onNuevaCambia: (String) -> Unit,
    onConfirmarCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onCambiar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(

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
                text = stringResource(
                    when (uiState.paso) {
                        1 -> R.string.indicacion_restablecer
                        2 -> R.string.indicacion_restablecer_respuesta
                        else -> R.string.indicacion_restablecer_nueva
                    }
                ),
                style = MaterialTheme.typography.bodyMedium
            )

            when (uiState.paso) {
                // ---- Paso 1: correo ----
                1 -> {
                    CampoTexto(
                        valor = uiState.correo,
                        onValorCambia = onCorreoCambia,
                        etiqueta = stringResource(R.string.campo_correo),
                        icono = R.drawable.ic_correo,
                        error = uiState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_continuar),
                        onClick = onContinuar,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---- Paso 2: pregunta de seguridad ----
                2 -> {
                    TarjetaSeccion(titulo = stringResource(R.string.campo_pregunta_seguridad)) {
                        Text(
                            text = uiState.pregunta,
                            style = MaterialTheme.typography.titleMedium
                        )
                        CampoTexto(
                            valor = uiState.respuesta,
                            onValorCambia = onRespuestaCambia,
                            etiqueta = stringResource(R.string.campo_respuesta),
                            error = uiState.errorRespuesta
                        )
                    }
                    BotonPrincipal(
                        texto = stringResource(R.string.btn_verificar_respuesta),
                        onClick = onVerificarRespuesta,
                        icono = R.drawable.ic_check,
                        modifier = Modifier.fillMaxWidth()
                    )
                    BotonSecundario(
                        texto = stringResource(R.string.btn_cambiar_correo),
                        onClick = onVolverAlCorreo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ---- Paso 3: contraseña nueva ----
                else -> {
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
                        habilitado = !uiState.cambioExitoso,
                        modifier = Modifier.fillMaxWidth()
                    )

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
    }
}


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

@Preview(showBackground = true, showSystemUi = true, name = "Paso 1: correo")
@Composable
private fun RestablecerCorreoPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(),
            onCorreoCambia = {}, onContinuar = {}, onRespuestaCambia = {}, onVerificarRespuesta = {},
            onVolverAlCorreo = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Paso 2: pregunta")
@Composable
private fun RestablecerPreguntaPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(
                paso = 2,
                correo = "tecnico@gmail.com",
                pregunta = "¿Cómo se llama tu primera mascota?",
                respuesta = "rex",
                errorRespuesta = "Respuesta incorrecta (te quedan 2 intentos)"
            ),
            onCorreoCambia = {}, onContinuar = {}, onRespuestaCambia = {}, onVerificarRespuesta = {},
            onVolverAlCorreo = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Paso 3: contraseña cambiada")
@Composable
private fun RestablecerExitoPreview() {
    InduztekTheme {
        RestablecerContrasenaContent(
            uiState = RestablecerUiState(
                paso = 3,
                correo = "tecnico@gmail.com",
                nueva = "lampa26",
                confirmar = "lampa26",
                cambioExitoso = true
            ),
            onCorreoCambia = {}, onContinuar = {}, onRespuestaCambia = {}, onVerificarRespuesta = {},
            onVolverAlCorreo = {}, onNuevaCambia = {}, onConfirmarCambia = {},
            onAlternarVisibilidad = {}, onCambiar = {}, onVolver = {}
        )
    }
}