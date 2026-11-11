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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.data.ConvertidorImagen
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.BotonSecundario
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.FotoEvidencia
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.formatearMedicion
import com.example.induztek_ingeniera_app.ui.styles.EstilosTarjeta
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProtocoloScreen(
    protocolo: Protocolo,
    onFormalizar: () -> Unit,
    onVerHistorial: () -> Unit,
    onVolver: () -> Unit
) {

    var mostrarDialogo by remember { mutableStateOf(false) }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text(stringResource(R.string.dialogo_formalizar_titulo)) },
            text = { Text(stringResource(R.string.dialogo_formalizar_mensaje, protocolo.codigoEquipo)) },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onFormalizar()
                }) { Text(stringResource(R.string.btn_formalizar)) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text(stringResource(R.string.dialogo_cancelar))
                }
            }
        )
    }

    val foto = remember(protocolo.foto) {
        protocolo.foto?.let { bytes -> ConvertidorImagen.aBitmap(bytes)?.asImageBitmap() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_detalle)) },
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
            // 1) Tarjeta principal: equipo, resultado/estado y datos de la prueba
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = EstilosTarjeta.forma,
                colors = EstilosTarjeta.colores(),
                elevation = EstilosTarjeta.elevacion()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.espacioMedio),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                ) {
                    Text(
                        text = "${protocolo.codigoEquipo} · ${protocolo.tipoEquipo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = protocolo.instalacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Resultado y estado con chips de color
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                        EtiquetaEstado(texto = protocolo.resultado.etiqueta, color = colorDe(protocolo.resultado))
                        EtiquetaEstado(texto = protocolo.estado.etiqueta, color = colorDe(protocolo.estado))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    FilaDato(
                        icono = Icons.Default.Description,
                        etiqueta = stringResource(R.string.etiqueta_prueba_tarjeta),
                        valor = protocolo.tipoPrueba
                    )
                    FilaDato(
                        icono = Icons.Default.Schedule,
                        etiqueta = stringResource(R.string.etiqueta_fecha_hora_tarjeta),
                        valor = formatearFecha(protocolo.fechaHora)
                    )
                    if (protocolo.nombreTecnico.isNotBlank()) {
                        FilaDato(
                            icono = Icons.Default.Build,
                            etiqueta = stringResource(R.string.etiqueta_tecnico_tarjeta),
                            valor = protocolo.nombreTecnico
                        )
                    }
                    FilaDato(
                        icono = Icons.Default.LocationOn,
                        etiqueta = stringResource(R.string.etiqueta_direccion_tarjeta),
                        valor = protocolo.direccionEquipo.ifBlank { stringResource(R.string.sin_direccion_registrada) }
                    )
                }
            }

            // 2) Mediciones: una fila por medición, con el valor destacado
            TarjetaSeccion(titulo = stringResource(R.string.seccion_mediciones)) {
                FilaMedicion(etiqueta = stringResource(R.string.campo_at_bt), valor = protocolo.atBt)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilaMedicion(etiqueta = stringResource(R.string.campo_at_tierra), valor = protocolo.atTierra)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilaMedicion(etiqueta = stringResource(R.string.campo_bt_tierra), valor = protocolo.btTierra)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                FilaMedicion(etiqueta = stringResource(R.string.campo_indice_pol), valor = protocolo.indicePolarizacion)
            }

            // 3) Observaciones (solo si hay)
            if (protocolo.observaciones.isNotBlank()) {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_observaciones)) {
                    Text(text = protocolo.observaciones, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // 4) Foto de evidencia que tomó el técnico
            TarjetaSeccion(titulo = stringResource(R.string.seccion_foto)) {
                if (foto != null) {
                    FotoEvidencia(foto = foto, descripcion = stringResource(R.string.cd_foto_evidencia))
                } else {
                    Text(
                        text = stringResource(R.string.sin_foto),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Comparar con las pruebas anteriores del mismo equipo
            BotonSecundario(
                texto = stringResource(R.string.btn_historial),
                onClick = onVerHistorial,
                modifier = Modifier.fillMaxWidth()
            )

            BotonPrincipal(
                texto = stringResource(R.string.btn_formalizar),
                onClick = { mostrarDialogo = true },
                icono = R.drawable.ic_check,
                habilitado = protocolo.estado == EstadoProtocolo.REGISTRADO_EN_TERRENO,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
// MEDICIÓN: nombre a la izquierda y el valor a la derecha dentro de una "píldora" de color
@Composable
private fun FilaMedicion(etiqueta: String, valor: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Text(
                text = formatearMedicion(valor),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioMini)
            )
        }
    }
}

// Protocolo de ejemplo para las vistas previas
private val protocoloPreview = Protocolo(
    equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
    atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
    resultado = Resultado.CON_OBSERVACION,
    observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
    estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
    codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
    instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas",
    direccionEquipo = "Camino Los Maitenes 1200, Lampa"
)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun DetalleRegistradoPreview() {
    InduztekTheme {
        DetalleProtocoloScreen(protocolo = protocoloPreview, onFormalizar = {}, onVerHistorial = {}, onVolver = {})
    }
}

@Preview(showBackground = true, heightDp = 1200, name = "Ya formalizado")
@Composable
private fun DetalleFormalizadoPreview() {
    InduztekTheme {
        DetalleProtocoloScreen(
            protocolo = protocoloPreview.copy(
                atBt = 13.1, atTierra = 9.4, btTierra = 7.8, indicePolarizacion = 2.4,
                resultado = Resultado.APROBADO, observaciones = "",
                estado = EstadoProtocolo.FORMALIZADO
            ),
            onFormalizar = {},
            onVerHistorial = {},
            onVolver = {}
        )
    }
}