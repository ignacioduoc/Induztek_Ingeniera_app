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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.ProtocoloUiState
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.GrupoRadio
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.ExitoFondo
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.NuevoProtocoloViewModel

// =====================================================================
//  NuevoProtocoloScreen.kt  —  PANTALLA 3: NUEVO PROTOCOLO (técnico)
//  ---------------------------------------------------------------------
//  El formulario que el técnico llena en terreno, dividido en tarjetas:
//     Equipo (datos de la BD) · Prueba · Mediciones · Resultado
//  Igual que el formulario del profe:
//   - NuevoProtocoloScreen  = se conecta con su ViewModel.
//   - NuevoProtocoloContent = solo dibuja (se puede ver con @Preview).
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
        onVolver = onVolver
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
    onVolver: () -> Unit
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

@Preview(showBackground = true, heightDp = 1400)
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

@Preview(showBackground = true, heightDp = 1400, name = "Con errores")
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