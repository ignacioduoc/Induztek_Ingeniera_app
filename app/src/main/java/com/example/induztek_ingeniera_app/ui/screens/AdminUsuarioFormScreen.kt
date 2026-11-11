package com.example.induztek_ingeniera_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.GrupoRadio
import com.example.induztek_ingeniera_app.ui.components.ListaDesplegable
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import com.example.induztek_ingeniera_app.viewmodel.AdminUsuariosViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuarioFormScreen(
    viewModel: AdminUsuariosViewModel,
    onVolver: () -> Unit
) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()
    val empresas by viewModel.empresas.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cuando el ViewModel avisa que se guardó: mensaje, se limpia la señal y se vuelve a la lista
    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            Toast.makeText(context, context.getString(R.string.usuario_guardado), Toast.LENGTH_SHORT).show()
            viewModel.limpiarGuardado()
            onVolver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (f.esEdicion) R.string.titulo_editar_usuario else R.string.titulo_nuevo_usuario
                        )
                    )
                },
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
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_personales)) {
                CampoTexto(
                    valor = f.nombre,
                    onValorCambia = viewModel::onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre),
                    error = f.errorNombre
                )
                CampoTexto(
                    valor = f.apellido,
                    onValorCambia = viewModel::onApellidoCambia,
                    etiqueta = stringResource(R.string.campo_apellido),
                    error = f.errorApellido
                )
                CampoTexto(
                    valor = f.rut,
                    // Se aceptan números, K, puntos y guion. Al guardar se deja como 12345678-5
                    onValorCambia = { viewModel.onRutCambia(it.filter { c -> c.isDigit() || c in "kK.-" }.take(12)) },
                    etiqueta = stringResource(R.string.campo_rut),
                    error = f.errorRut
                )
                CampoTexto(
                    valor = f.telefono,
                    onValorCambia = { viewModel.onTelefonoCambia(it.filter { c -> c.isDigit() }.take(9)) },
                    etiqueta = stringResource(R.string.campo_telefono),
                    error = f.errorTelefono,
                    tipoTeclado = KeyboardType.Number,
                    prefijo = "+56"
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_cuenta)) {
                CampoTexto(
                    valor = f.correo,
                    onValorCambia = viewModel::onCorreoCambia,
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    error = f.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                // Rol: se elige solo al crear. Al editar queda fijo (así nadie se quita el rol de administrador por error)
                if (f.esEdicion) {
                    Text(
                        text = stringResource(R.string.rol_fijo, nombreDeRol(f.idRol)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(stringResource(R.string.etiqueta_rol), style = MaterialTheme.typography.labelLarge)
                    GrupoRadio(
                        opciones = listOf(Rol.ID_TECNICO, Rol.ID_CLIENTE),
                        seleccionada = f.idRol,
                        onSeleccionar = viewModel::onRolCambia,
                        textoDe = { nombreDeRol(it) }
                    )
                }

                ListaDesplegable(
                    etiqueta = stringResource(R.string.etiqueta_empresa_usuario),
                    textoSeleccionado = empresas.firstOrNull { it.idEmpresa == f.idEmpresa }?.nombre ?: "",
                    opciones = empresas,
                    textoDe = { it.nombre },
                    onElegir = { viewModel.onEmpresaCambia(it.idEmpresa) },
                    error = f.errorEmpresa
                )

                CampoContrasena(
                    valor = f.contrasena,
                    onValorCambia = viewModel::onContrasenaCambia,
                    etiqueta = stringResource(
                        if (f.esEdicion) R.string.campo_contrasena_opcional else R.string.campo_contrasena
                    ),
                    visible = f.contrasenaVisible,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = f.errorContrasena
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
                    etiqueta = stringResource(
                        if (f.esEdicion) R.string.campo_respuesta_opcional else R.string.campo_respuesta
                    ),
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
                texto = stringResource(R.string.btn_guardar_usuario),
                onClick = viewModel::onGuardar,
                icono = R.drawable.ic_check,
                habilitado = !f.guardando && !f.cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


private fun nombreDeRol(idRol: Long): String = when (idRol) {
    Rol.ID_TECNICO -> "Técnico"
    Rol.ID_CLIENTE -> "Cliente"
    Rol.ID_ADMINISTRADOR -> "Administrador"
    else -> "Usuario"
}