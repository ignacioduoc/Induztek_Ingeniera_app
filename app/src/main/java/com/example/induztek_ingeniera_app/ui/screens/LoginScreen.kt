package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.LoginUiState
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.ExitoFondo
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel


@Composable
fun LoginScreen(
    onLoginExitoso: (Usuario) -> Unit,
    onOlvideContrasena: () -> Unit,          // NUEVO: abre "Restablecer contraseña"
    viewModel: LoginViewModel
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    LaunchedEffect(uiState.usuarioIngresado) {
        val usuario = uiState.usuarioIngresado
        if (usuario != null) {
            onLoginExitoso(usuario)
        }
    }

    // 3. El contenido solo dibuja (sirve para las vistas previas)
    LoginContent(
        uiState = uiState,
        onCorreoCambia = viewModel::onCorreoCambia,
        onContrasenaCambia = viewModel::onContrasenaCambia,
        onAlternarVisibilidad = viewModel::onAlternarVisibilidad,
        onIngresar = { viewModel.onIngresar() },
        onOlvideContrasena = onOlvideContrasena
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCorreoCambia: (String) -> Unit,
    onContrasenaCambia: (String) -> Unit,
    onAlternarVisibilidad: () -> Unit,
    onIngresar: () -> Unit,
    onOlvideContrasena: () -> Unit
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Spacer(Modifier.height(Dimens.espacioGrande))

            Text(
                text = stringResource(R.string.titulo_login),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.subtitulo_login),
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
            CampoContrasena(
                valor = uiState.contrasena,
                onValorCambia = onContrasenaCambia,
                etiqueta = stringResource(R.string.campo_contrasena),
                visible = uiState.contrasenaVisible,
                onAlternarVisible = onAlternarVisibilidad,
                error = uiState.errorContrasena
            )

            // Error de la BD: los campos están bien escritos, pero no existe ese usuario
            uiState.errorCredenciales?.let { mensaje ->
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_ingresar),
                onClick = onIngresar,
                modifier = Modifier.fillMaxWidth()
            )


            TextButton(onClick = onOlvideContrasena) {
                Text(stringResource(R.string.btn_olvide_contrasena))
            }


            if (uiState.usuarioIngresado != null) {
                MensajeBienvenida(nombre = uiState.usuarioIngresado.nombre)
            }

            Text(
                text = stringResource(R.string.nota_login),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun MensajeBienvenida(nombre: String) {
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
                text = stringResource(R.string.saludo, nombre),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginVacioPreview() {
    InduztekTheme {
        LoginContent(
            uiState = LoginUiState(),
            onCorreoCambia = {}, onContrasenaCambia = {},
            onAlternarVisibilidad = {}, onIngresar = {}, onOlvideContrasena = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Con errores")
@Composable
private fun LoginConErroresPreview() {
    InduztekTheme {
        LoginContent(
            uiState = LoginUiState(
                correo = "tecnico@",
                errorCorreo = "Ingresa un correo válido",
                errorContrasena = "Ingresa tu contraseña"
            ),
            onCorreoCambia = {}, onContrasenaCambia = {},
            onAlternarVisibilidad = {}, onIngresar = {}, onOlvideContrasena = {}
        )
    }
}