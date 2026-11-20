package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroClienteScreen(
    viewModel: LoginViewModel,
    onVolver: () -> Unit
) {
    val state by viewModel.registroState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registroExitoso) {
        if (state.registroExitoso) {
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro de Cliente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            painter = painterResource(R.drawable.ic_volver),
                            contentDescription = "Volver"
                        )
                    }
                }
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
            Text(
                text = "Crea tu cuenta de cliente para consultar el estado y protocolos de tus equipos.",
                style = MaterialTheme.typography.bodyMedium
            )

            CampoTexto(
                valor = state.nombre,
                onValorCambia = viewModel::onRegNombreCambia,
                etiqueta = "Nombre",
                error = state.errorNombre
            )

            CampoTexto(
                valor = state.apellido,
                onValorCambia = viewModel::onRegApellidoCambia,
                etiqueta = "Apellido",
                error = state.errorApellido
            )

            CampoTexto(
                valor = state.rut,
                onValorCambia = viewModel::onRegRutCambia,
                etiqueta = "RUT (ej. 12.345.678-9)",
                error = state.errorRut
            )

            CampoTexto(
                valor = state.telefono,
                onValorCambia = viewModel::onRegTelefonoCambia,
                etiqueta = "Teléfono (opcional)",
                tipoTeclado = KeyboardType.Phone
            )

            CampoTexto(
                valor = state.correo,
                onValorCambia = viewModel::onRegCorreoCambia,
                etiqueta = "Correo electrónico",
                icono = R.drawable.ic_correo,
                error = state.errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            CampoContrasena(
                valor = state.contrasena,
                onValorCambia = viewModel::onRegContrasenaCambia,
                etiqueta = "Contraseña (mín. 6 caracteres)",
                visible = state.contrasenaVisible,
                onAlternarVisible = viewModel::onRegAlternarVisibilidad,
                error = state.errorContrasena
            )

            CampoContrasena(
                valor = state.confirmarContrasena,
                onValorCambia = viewModel::onRegConfirmarCambia,
                etiqueta = "Confirmar contraseña",
                visible = state.contrasenaVisible,
                onAlternarVisible = viewModel::onRegAlternarVisibilidad,
                error = state.errorConfirmar
            )

            CampoTexto(
                valor = state.preguntaSeguridad,
                onValorCambia = viewModel::onRegPreguntaCambia,
                etiqueta = "Pregunta de seguridad (ej. Nombre de tu mascota)",
                error = state.errorPregunta
            )

            CampoTexto(
                valor = state.respuestaSeguridad,
                onValorCambia = viewModel::onRegRespuestaCambia,
                etiqueta = "Respuesta de seguridad",
                error = state.errorRespuesta
            )

            state.errorGeneral?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = "Crear cuenta de cliente",
                onClick = viewModel::registrarCliente,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("¿Ya tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onVolver) {
                    Text("Inicia sesión")
                }
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}