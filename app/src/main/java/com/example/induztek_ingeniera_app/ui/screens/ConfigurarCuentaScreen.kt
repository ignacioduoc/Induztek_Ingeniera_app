package com.example.induztek_ingeniera_app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.BotonSecundario
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.ListaDesplegable
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import com.example.induztek_ingeniera_app.viewmodel.PerfilViewModel


/**
 * Primer ingreso (o cuenta reiniciada por el administrador): el usuario entró con una contraseña
 * temporal y debe crear la suya y elegir su pregunta de seguridad. No se puede saltar.
 * Reutiliza el PerfilViewModel y los mismos campos que el perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurarCuentaScreen(
    usuario: Usuario?,
    viewModel: PerfilViewModel,
    onListo: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()

    // Sin esto, el botón "atrás" dejaría entrar sin configurar la cuenta
    BackHandler(enabled = true) { }

    // Al entrar se cargan los datos del usuario que inició sesión
    LaunchedEffect(usuario?.id) {
        usuario?.let { viewModel.abrirPerfil(it.id) }
    }

    // Cuando se guardó: se limpia la señal y se continúa al inicio
    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            viewModel.limpiarGuardado()
            onListo()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_configurar_cuenta)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            Text(
                text = stringResource(R.string.indicacion_configurar_cuenta),
                style = MaterialTheme.typography.bodyMedium
            )

            TarjetaSeccion(titulo = stringResource(R.string.campo_nueva_contrasena)) {
                CampoContrasena(
                    valor = f.contrasenaNueva,
                    onValorCambia = viewModel::onContrasenaNuevaCambia,
                    etiqueta = stringResource(R.string.campo_nueva_contrasena),
                    visible = f.contrasenasVisibles,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = f.errorContrasenaNueva
                )
                CampoContrasena(
                    valor = f.contrasenaConfirmar,
                    onValorCambia = viewModel::onContrasenaConfirmarCambia,
                    etiqueta = stringResource(R.string.campo_confirmar_contrasena),
                    visible = f.contrasenasVisibles,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = f.errorContrasenaConfirmar
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_seguridad)) {
                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_pregunta_seguridad),
                    textoSeleccionado = f.pregunta,
                    opciones = Validaciones.PREGUNTAS_SEGURIDAD,
                    textoDe = { it },
                    onElegir = viewModel::onPreguntaCambia,
                    error = f.errorPregunta
                )
                CampoTexto(
                    valor = f.respuesta,
                    onValorCambia = viewModel::onRespuestaCambia,
                    etiqueta = stringResource(R.string.campo_respuesta),
                    error = f.errorRespuesta
                )
            }

            if (f.errorGeneral != null) {
                Text(
                    text = f.errorGeneral!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_continuar),
                onClick = viewModel::onConfigurarCuenta,
                icono = R.drawable.ic_check,
                habilitado = !f.guardando && !f.cargando,
                modifier = Modifier.fillMaxWidth()
            )

            // Salida: si no quiere configurar ahora, cierra sesión (no puede usar la app sin hacerlo)
            BotonSecundario(
                texto = stringResource(R.string.menu_cerrar_sesion),
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}