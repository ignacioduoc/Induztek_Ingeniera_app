package com.example.induztek_ingeniera_app.ui.screens

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.data.ConvertidorImagen
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.ProtocoloUiState
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.permisos.Permisos
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.FotoEvidencia
import com.example.induztek_ingeniera_app.ui.components.GrupoRadio
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.ExitoFondo
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.NuevoProtocoloViewModel
import kotlinx.coroutines.launch

// =====================================================================
//  NuevoProtocoloScreen.kt  —  PANTALLA 3: NUEVO PROTOCOLO (técnico)
//  ---------------------------------------------------------------------
//  El formulario que el técnico llena en terreno, dividido en tarjetas:
//     Equipo (datos de la BD) · Prueba · Mediciones · Resultado · Foto
//  Igual que el formulario del profe:
//   - NuevoProtocoloScreen  = se conecta con su ViewModel y con la cámara.
//   - NuevoProtocoloContent = solo dibuja (se puede ver con @Preview).
//  La foto usa los 4 pasos de los permisos del profe (ver Permisos.kt).
// =====================================================================

@Composable
fun NuevoProtocoloScreen(
    equipo: Equipo,
    tecnicoId: Long,
    onGuardar: (Protocolo) -> Unit,
    onVolver: () -> Unit,
    viewModel: NuevoProtocoloViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Context = acceso a los servicios de Android (permisos, Ajustes...)
    val context = LocalContext.current

    // Snackbar para avisar cuando el técnico rechaza el permiso.
    // stringResource solo funciona en un @Composable: los textos se preparan aquí.
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val textoCamaraDenegada = stringResource(R.string.permiso_camara_denegado)
    val textoAjustes = stringResource(R.string.btn_ajustes)

    // PASO 4 (rechazo) -> avisar y ofrecer abrir Ajustes.
    // Si el técnico rechazó 2 veces, Android ya no muestra el diálogo: solo queda Ajustes.
    fun avisarPermisoDenegado(mensaje: String) {
        scope.launch {
            val resultado = snackbarHostState.showSnackbar(
                message = mensaje,
                actionLabel = textoAjustes,
                duration = SnackbarDuration.Long
            )
            if (resultado == SnackbarResult.ActionPerformed) Permisos.abrirAjustes(context)
        }
    }

    // "launcher" = lanzador de OTRA app que nos devuelve un resultado.
    // TakePicturePreview abre la app de cámara y entrega un Bitmap (o null si canceló).
    // Como la app DECLARA el permiso CAMERA, Android exige que esté CONCEDIDO
    // antes de abrir la cámara (si no, lanza SecurityException).
    val lanzadorCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { foto: Bitmap? ->
        if (foto != null) viewModel.onFotoTomada(foto)
    }

    // Se llama SOLO con el permiso ya concedido.
    // try/catch: si el equipo no tiene app de cámara, la app no se cae.
    fun abrirCamara() {
        try {
            lanzadorCamara.launch(null)
        } catch (e: ActivityNotFoundException) {
        }
    }

    // PASO 3 -> PEDIR. RequestPermission muestra el diálogo del sistema
    // "¿Permitir que Induztek Protocolos tome fotos y grabe videos?" y entrega true o false.
    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        // PASO 4 -> REACCIONAR a la respuesta del técnico
        if (concedido) abrirCamara() else avisarPermisoDenegado(textoCamaraDenegada)
    }

    // Bytes del estado -> Bitmap -> ImageBitmap (el tipo que usa Compose).
    // remember(uiState.foto): se convierte solo cuando la foto CAMBIA, no en cada dibujo.
    val foto = remember(uiState.foto) {
        uiState.foto?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    NuevoProtocoloContent(
        equipo = equipo,
        uiState = uiState,
        onTipoPruebaSeleccionado = viewModel::onTipoPruebaSeleccionado,
        onAtBtCambia = viewModel::onAtBtCambia,                                 // MEDICIÓN
        onAtTierraCambia = viewModel::onAtTierraCambia,                         // MEDICIÓN
        onBtTierraCambia = viewModel::onBtTierraCambia,                         // MEDICIÓN
        onIndicePolarizacionCambia = viewModel::onIndicePolarizacionCambia,     // MEDICIÓN
        onResultadoSeleccionado = viewModel::onResultadoSeleccionado,
        onObservacionesCambia = viewModel::onObservacionesCambia,
        // Si el formulario está bien, el Protocolo armado se entrega hacia afuera
        onGuardar = { viewModel.onGuardar(equipo.id, tecnicoId)?.let { protocolo -> onGuardar(protocolo) } },
        onVolver = onVolver,
        foto = foto,
        snackbarHostState = snackbarHostState,
        onTomarFoto = {
            // PASO 2 -> VERIFICAR. ¿Ya tengo permiso? Uso la cámara.
            // ¿No? Lo pido (PASO 3) y la respuesta llega a pedirPermisoCamara (PASO 4).
            if (Permisos.tiene(context, Permisos.CAMARA)) abrirCamara()
            else pedirPermisoCamara.launch(Permisos.CAMARA)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevoProtocoloContent(
    equipo: Equipo,
    uiState: ProtocoloUiState,
    onTipoPruebaSeleccionado: (String) -> Unit,
    onAtBtCambia: (String) -> Unit,
    onAtTierraCambia: (String) -> Unit,
    onBtTierraCambia: (String) -> Unit,
    onIndicePolarizacionCambia: (String) -> Unit,
    onResultadoSeleccionado: (Resultado) -> Unit,
    onObservacionesCambia: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    foto: ImageBitmap? = null,             // "= ..." -> los @Preview no cambian
    onTomarFoto: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_nuevo_protocolo)) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },   // aquí aparece el aviso del permiso
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
            // 1) Equipo: datos que vienen de la BD (no se escriben)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_equipo)) {
                Text(text = "${equipo.codigo} · ${equipo.tipo}", style = MaterialTheme.typography.bodyMedium)
                Text(text = equipo.empresaCliente, style = MaterialTheme.typography.bodyMedium)
                Text(text = equipo.instalacion, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.fecha_hora, formatearFecha(uiState.fechaHora)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 2) Prueba: lista desplegable, igual que "Ciudad" en el formulario del profe
            TarjetaSeccion(titulo = stringResource(R.string.seccion_prueba)) {
                // ¿La lista está abierta? Es estado de la pantalla, no del ViewModel.
                var expandido by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expandido,
                    onExpandedChange = { expandido = it }
                ) {
                    OutlinedTextField(
                        value = uiState.tipoPrueba,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.campo_tipo_prueba)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        shape = EstilosCampo.forma,
                        colors = EstilosCampo.colores(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandido,
                        onDismissRequest = { expandido = false }
                    ) {
                        uiState.tiposPrueba.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    onTipoPruebaSeleccionado(tipo)
                                    expandido = false
                                }
                            )
                        }
                    }
                }
            }

            // 3) MEDICIÓN: un campo por medición, con teclado de números con decimales
            TarjetaSeccion(titulo = stringResource(R.string.seccion_mediciones)) {
                CampoTexto(
                    valor = uiState.atBt,
                    onValorCambia = onAtBtCambia,
                    etiqueta = stringResource(R.string.campo_at_bt),
                    icono = R.drawable.ic_bar_chart,
                    error = uiState.errorAtBt,
                    tipoTeclado = KeyboardType.Decimal
                )
                CampoTexto(
                    valor = uiState.atTierra,
                    onValorCambia = onAtTierraCambia,
                    etiqueta = stringResource(R.string.campo_at_tierra),
                    icono = R.drawable.ic_bar_chart,
                    error = uiState.errorAtTierra,
                    tipoTeclado = KeyboardType.Decimal
                )
                CampoTexto(
                    valor = uiState.btTierra,
                    onValorCambia = onBtTierraCambia,
                    etiqueta = stringResource(R.string.campo_bt_tierra),
                    icono = R.drawable.ic_bar_chart,
                    error = uiState.errorBtTierra,
                    tipoTeclado = KeyboardType.Decimal
                )
                CampoTexto(
                    valor = uiState.indicePolarizacion,
                    onValorCambia = onIndicePolarizacionCambia,
                    etiqueta = stringResource(R.string.campo_indice_pol),
                    icono = R.drawable.ic_bar_chart,
                    error = uiState.errorIndicePolarizacion,
                    tipoTeclado = KeyboardType.Decimal
                )
            }

            // 4) Resultado (RadioButton) y observaciones
            TarjetaSeccion(titulo = stringResource(R.string.seccion_resultado)) {
                GrupoRadio(
                    opciones = Resultado.entries,
                    seleccionada = uiState.resultado,
                    onSeleccionar = onResultadoSeleccionado,
                    textoDe = { it.etiqueta }
                )
                // Sin ícono: CampoTexto ahora lo permite (icono = null por defecto)
                CampoTexto(
                    valor = uiState.observaciones,
                    onValorCambia = onObservacionesCambia,
                    etiqueta = stringResource(R.string.campo_observaciones),
                    error = uiState.errorObservaciones
                )
            }

            // 5) Foto de evidencia (cámara). Es opcional.
            TarjetaSeccion(titulo = stringResource(R.string.seccion_foto)) {
                if (foto != null) {
                    // Esquinas redondeadas + tamaño ajustado (ver FotoEvidencia.kt)
                    FotoEvidencia(foto = foto, descripcion = stringResource(R.string.cd_foto_evidencia))
                } else {
                    Text(text = stringResource(R.string.texto_sin_foto), style = MaterialTheme.typography.bodyMedium)
                }
                // Igual que "Guardar": una vez guardado, la foto ya no se cambia
                BotonConIcono(
                    icono = R.drawable.ic_camara,
                    texto = stringResource(if (foto == null) R.string.btn_tomar_foto else R.string.btn_otra_foto),
                    onClick = onTomarFoto,
                    habilitado = !uiState.guardado
                )
            }

            // Una vez guardado, el botón se bloquea para no guardar dos veces
            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_protocolo),
                onClick = onGuardar,
                icono = R.drawable.ic_check,
                habilitado = !uiState.guardado,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.guardado) {
                MensajeGuardado()
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

// Botón con contorno, ícono y texto (el BotonConIcono del PerfilScreen del profe).
// Privado: solo se usa aquí.
@Composable
private fun BotonConIcono(
    icono: Int,
    texto: String,
    onClick: () -> Unit,
    habilitado: Boolean = true      // CAMBIO (Bloquear al guardar): igual que en BotonPrincipal
) {
    OutlinedButton(onClick = onClick, enabled = habilitado) {
        Icon(painterResource(icono), contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
        Spacer(Modifier.width(Dimens.espacioChico))
        Text(texto)
    }
}

// Tarjeta verde de confirmación, igual que el MensajeExito del formulario del profe
@Composable
private fun MensajeGuardado() {
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
                text = stringResource(R.string.protocolo_guardado),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = Dimens.espacioChico)
            )
        }
    }
}

// Equipo de ejemplo para las vistas previas (el mismo de la BD)
private val equipoPreview = Equipo(
    "TR-0142", "Transformador 500 kVA", "Planta Lampa · S/E-02",
    clienteId = 2, id = 1, empresaCliente = "Industrias Aurora S.A."
)

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun NuevoProtocoloVacioPreview() {
    InduztekTheme {
        NuevoProtocoloContent(
            equipo = equipoPreview,
            uiState = ProtocoloUiState(),
            onTipoPruebaSeleccionado = {}, onAtBtCambia = {}, onAtTierraCambia = {},
            onBtTierraCambia = {}, onIndicePolarizacionCambia = {}, onResultadoSeleccionado = {},
            onObservacionesCambia = {}, onGuardar = {}, onVolver = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500, name = "Con errores")
@Composable
private fun NuevoProtocoloConErroresPreview() {
    InduztekTheme {
        NuevoProtocoloContent(
            equipo = equipoPreview,
            uiState = ProtocoloUiState(
                atBt = "12,4",
                btTierra = "abc",
                indicePolarizacion = "0",
                resultado = Resultado.CON_OBSERVACION,
                errorAtTierra = "Ingresa la medición",
                errorBtTierra = "Ingresa un número, por ejemplo 12,4",
                errorIndicePolarizacion = "Debe ser mayor que 0",
                errorObservaciones = "Explica la observación o el motivo del rechazo"
            ),
            onTipoPruebaSeleccionado = {}, onAtBtCambia = {}, onAtTierraCambia = {},
            onBtTierraCambia = {}, onIndicePolarizacionCambia = {}, onResultadoSeleccionado = {},
            onObservacionesCambia = {}, onGuardar = {}, onVolver = {}
        )
    }
}