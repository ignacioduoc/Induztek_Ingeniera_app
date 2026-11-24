package com.example.induztek_ingeniera_app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.OutlinedButton
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
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.GrupoRadio
import com.example.induztek_ingeniera_app.ui.components.ListaDesplegable
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.viewmodel.AdminUsuariosViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsuarioFormScreen(
    viewModel: AdminUsuariosViewModel,
    esSupervisor: Boolean = false,
    onVolver: () -> Unit
) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()
    val empresas by viewModel.empresas.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            val claveGenerada = viewModel.consumirClaveGeneradaParaCorreo()
            if (!claveGenerada.isNullOrBlank()) {
                enviarCorreoCredenciales(
                    context = context,
                    destinatario = f.correo.trim(),
                    nombre = f.nombre.trim(),
                    claveTemporal = claveGenerada
                )
            }
            Toast.makeText(
                context,
                "Usuario guardado. Se generó la clave automática y se preparó el envío por correo.",
                Toast.LENGTH_LONG
            ).show()
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

                if (f.esEdicion && f.idRol == Rol.ID_ADMINISTRADOR) {
                    Text(
                        text = stringResource(R.string.rol_fijo, nombreDeRol(f.idRol)),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(stringResource(R.string.etiqueta_rol), style = MaterialTheme.typography.labelLarge)
                    val rolesPermitidos = if (esSupervisor) {
                        listOf(Rol.ID_TECNICO, Rol.ID_CLIENTE)
                    } else {
                        listOf(Rol.ID_TECNICO, Rol.ID_CLIENTE, Rol.ID_SUPERVISOR)
                    }
                    GrupoRadio(
                        opciones = rolesPermitidos,
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

                // Seguridad: ni Admin ni Supervisor escriben o visualizan la contraseña
                Text(
                    text = if (f.esEdicion) {
                        "Por seguridad, la contraseña actual es confidencial y no se muestra."
                    } else {
                        "Seguridad: Se generará una contraseña aleatoria oculta y se enviará directamente al correo del usuario."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (f.esEdicion) {
                    OutlinedButton(
                        onClick = { viewModel.onReiniciarClaveAleatoria() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generar y enviar nueva contraseña aleatoria por correo")
                    }
                }
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

private fun enviarCorreoCredenciales(
    context: Context,
    destinatario: String,
    nombre: String,
    claveTemporal: String
) {
    val asunto = "Credenciales de acceso - Induztek Ingeniería"
    val cuerpo = """
        Hola $nombre,
        
        Se ha creado o actualizado tu cuenta en la aplicación móvil de Induztek Ingeniería.
        
        Usuario: $destinatario
        Contraseña temporal: $claveTemporal
        
        Por seguridad, al iniciar sesión por primera vez el sistema te pedirá cambiar esta contraseña y configurar tu pregunta de seguridad.
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
        putExtra(Intent.EXTRA_SUBJECT, asunto)
        putExtra(Intent.EXTRA_TEXT, cuerpo)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Enviar credenciales por correo"))
    } catch (_: Exception) {
        Toast.makeText(context, "No hay app de correo configurada en el dispositivo", Toast.LENGTH_SHORT).show()
    }
}

private fun nombreDeRol(idRol: Long): String = when (idRol) {
    Rol.ID_TECNICO -> "Técnico"
    Rol.ID_CLIENTE -> "Cliente"
    Rol.ID_SUPERVISOR -> "Supervisor"
    Rol.ID_ADMINISTRADOR -> "Administrador"
    else -> "Usuario"
}