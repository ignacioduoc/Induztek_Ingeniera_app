package com.example.induztek_ingeniera_app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoContrasena
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.ListaDesplegable
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import com.example.induztek_ingeniera_app.viewmodel.PerfilViewModel
import java.io.ByteArrayOutputStream
import java.io.File


private fun comprimirYRedimensionarBitmap(bitmapOriginal: Bitmap, maxDim: Int = 512): ByteArray {
    val width = bitmapOriginal.width
    val height = bitmapOriginal.height
    val ratio = width.toFloat() / height.toFloat()

    val newWidth: Int
    val newHeight: Int
    if (width > height) {
        newWidth = maxDim
        newHeight = (maxDim / ratio).toInt()
    } else {
        newHeight = maxDim
        newWidth = (maxDim * ratio).toInt()
    }

    val scaledBitmap = Bitmap.createScaledBitmap(bitmapOriginal, newWidth, newHeight, true)

    val softwareBitmap = try {
        scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
    } catch (e: Exception) {
        scaledBitmap
    }

    val stream = ByteArrayOutputStream()
    softwareBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return stream.toByteArray()
}


/**
 * Perfil de cualquier rol (técnico, cliente o administrador).
 * Editable: foto, nombre, apellido, teléfono, pregunta de seguridad y contraseña.
 * Solo lectura: RUT, correo y empresa (los cambia el administrador).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    viewModel: PerfilViewModel,
    onPerfilGuardado: () -> Unit,        // avisa para refrescar el nombre y la foto del inicio
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    val f by viewModel.formulario.collectAsStateWithLifecycle()

    // Al entrar se cargan los datos del usuario que inició sesión
    LaunchedEffect(usuario?.id) {
        usuario?.let { viewModel.abrirPerfil(it.id) }
    }

    // Foto: se muestra la actual; si elige otra, se guarda al apretar "Guardar cambios"
    var bitmapFoto by remember(usuario?.id) {
        mutableStateOf<Bitmap?>(
            usuario?.fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        )
    }
    var fotoCambiada by remember(usuario?.id) { mutableStateOf(false) }
    var mostrarDialogoOrigen by remember { mutableStateOf(false) }
    var photoUriTemp by remember { mutableStateOf<Uri?>(null) }

    // Cuando el ViewModel avisa que se guardó: mensaje, se limpia la señal, se refresca el inicio y se vuelve
    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            Toast.makeText(context, context.getString(R.string.perfil_guardado), Toast.LENGTH_SHORT).show()
            viewModel.limpiarGuardado()
            onPerfilGuardado()
            onVolver()
        }
    }

    // Crea el archivo temporal donde la cámara deja la foto
    fun crearUriFoto(): Uri? {
        return try {
            val file = File.createTempFile("avatar_capture_", ".jpg", context.cacheDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            null
        }
    }

    fun cargarBitmap(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ -> decoder.isMutableRequired = true }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }

    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        val uri = photoUriTemp
        if (exito && uri != null) {
            try {
                bitmapFoto = cargarBitmap(uri)
                fotoCambiada = true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_foto_procesar), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearUriFoto()
            if (uri != null) {
                photoUriTemp = uri
                launcherCamara.launch(uri)
            } else {
                Toast.makeText(context, context.getString(R.string.error_foto_archivo), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, context.getString(R.string.permiso_camara_denegado), Toast.LENGTH_SHORT).show()
        }
    }

    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                bitmapFoto = cargarBitmap(it)
                fotoCambiada = true
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_foto_cargar), Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (mostrarDialogoOrigen) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOrigen = false },
            title = { Text(stringResource(R.string.dialogo_foto_titulo)) },
            text = { Text(stringResource(R.string.dialogo_foto_mensaje)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                        val uri = crearUriFoto()
                        if (uri != null) {
                            photoUriTemp = uri
                            launcherCamara.launch(uri)
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_foto_archivo), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_camara))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    launcherGaleria.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_galeria))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_editar_perfil)) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {

            // ---- Foto de perfil ----
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(130.dp)
                    .clickable { mostrarDialogoOrigen = true }
            ) {
                val foto = bitmapFoto
                if (foto != null) {
                    Image(
                        bitmap = foto.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_foto_perfil),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f.nombre.take(1).uppercase().ifBlank { "U" },
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.cd_cambiar_foto),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            }

            // ---- Datos personales ----
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
                    onValorCambia = {},
                    etiqueta = stringResource(R.string.etiqueta_rut_tarjeta),
                    habilitado = false,
                    ayuda = stringResource(R.string.ayuda_solo_admin)
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

            // ---- Cuenta (solo lectura) ----
            TarjetaSeccion(titulo = stringResource(R.string.seccion_cuenta)) {
                CampoTexto(
                    valor = f.correo,
                    onValorCambia = {},
                    etiqueta = stringResource(R.string.campo_correo),
                    icono = R.drawable.ic_correo,
                    habilitado = false,
                    ayuda = stringResource(R.string.ayuda_solo_admin)
                )
                CampoTexto(
                    valor = f.empresa.ifBlank { stringResource(R.string.sin_empresa) },
                    onValorCambia = {},
                    etiqueta = stringResource(R.string.etiqueta_empresa_tarjeta),
                    habilitado = false,
                    ayuda = stringResource(R.string.ayuda_solo_admin)
                )
            }

            // ---- Seguridad ----
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
                    etiqueta = stringResource(R.string.campo_respuesta_opcional),
                    error = f.errorRespuesta
                )
            }

            // ---- Cambiar contraseña (opcional) ----
            TarjetaSeccion(titulo = stringResource(R.string.seccion_cambiar_contrasena)) {
                Text(
                    text = stringResource(R.string.indicacion_cambiar_contrasena),
                    style = MaterialTheme.typography.bodySmall
                )
                CampoContrasena(
                    valor = f.contrasenaActual,
                    onValorCambia = viewModel::onContrasenaActualCambia,
                    etiqueta = stringResource(R.string.campo_contrasena_actual),
                    visible = f.contrasenasVisibles,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = f.errorContrasenaActual
                )
                CampoContrasena(
                    valor = f.contrasenaNueva,
                    onValorCambia = viewModel::onContrasenaNuevaCambia,
                    etiqueta = stringResource(R.string.campo_contrasena_nueva),
                    visible = f.contrasenasVisibles,
                    onAlternarVisible = viewModel::onAlternarVisibilidad,
                    error = f.errorContrasenaNueva
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
                texto = stringResource(R.string.btn_guardar_cambios),
                onClick = {
                    // Solo se envía la foto si eligió una distinta; si no, queda la que tenía
                    val fotoNueva = if (fotoCambiada) bitmapFoto?.let { comprimirYRedimensionarBitmap(it) } else null
                    viewModel.onGuardar(fotoNueva)
                },
                icono = R.drawable.ic_check,
                habilitado = !f.guardando && !f.cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}