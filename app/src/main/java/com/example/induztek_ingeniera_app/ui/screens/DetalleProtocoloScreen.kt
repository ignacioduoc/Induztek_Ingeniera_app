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
import androidx.compose.material3.AlertDialog
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
import com.example.induztek_ingeniera_app.ui.components.FotoEvidencia
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.formatearMedicion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

// =====================================================================
//  DetalleProtocoloScreen.kt  —  PANTALLA 5: DETALLE DEL PROTOCOLO (cliente)
//  ---------------------------------------------------------------------
//  Muestra el protocolo completo leído de la BD (con JOIN): equipo, prueba,
//  técnico, mediciones, resultado, estado y la foto de evidencia.
//  Botón "Ver historial del equipo" = compara con las pruebas anteriores.
//  Botón "Formalizar protocolo" = UPDATE del estado. Antes pide confirmación
//  con un AlertDialog (igual que "Eliminar" en la lista del profe), porque
//  no se puede deshacer. Si ya está formalizado, el botón queda bloqueado.
//  No tiene ViewModel propio: recibe el protocolo y avisa con lambdas.
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProtocoloScreen(
    protocolo: Protocolo,
    onFormalizar: () -> Unit,
    onVerHistorial: () -> Unit,
    onVolver: () -> Unit
) {
    // ¿Se muestra el diálogo "¿Formalizar el protocolo?"? Es estado de la pantalla.
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

    // Bytes de la BD -> Bitmap -> ImageBitmap (igual que en el formulario).
    // null = el técnico no tomó foto.
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
            // 1) Equipo: código, tipo e instalación llegan por el JOIN con la tabla equipos
            TarjetaSeccion(titulo = stringResource(R.string.seccion_equipo)) {
                Text(text = "${protocolo.codigoEquipo} · ${protocolo.tipoEquipo}", style = MaterialTheme.typography.bodyMedium)
                Text(text = protocolo.instalacion, style = MaterialTheme.typography.bodyMedium)
            }

            // 2) Prueba: qué se probó, cuándo y quién (el nombre llega por el JOIN con usuarios)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_prueba)) {
                Text(text = protocolo.tipoPrueba, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.fecha_hora, formatearFecha(protocolo.fechaHora)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.tecnico_nombre, protocolo.nombreTecnico),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 3) MEDICIÓN: una fila por medición, con los mismos nombres del formulario
            TarjetaSeccion(titulo = stringResource(R.string.seccion_mediciones)) {
                FilaMedicion(etiqueta = stringResource(R.string.campo_at_bt), valor = protocolo.atBt)
                FilaMedicion(etiqueta = stringResource(R.string.campo_at_tierra), valor = protocolo.atTierra)
                FilaMedicion(etiqueta = stringResource(R.string.campo_bt_tierra), valor = protocolo.btTierra)
                FilaMedicion(etiqueta = stringResource(R.string.campo_indice_pol), valor = protocolo.indicePolarizacion)
            }

            // 4) Resultado y estado con chips de color + observaciones (solo si hay)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_resultado)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    EtiquetaEstado(texto = protocolo.resultado.etiqueta, color = colorDe(protocolo.resultado))
                    EtiquetaEstado(texto = protocolo.estado.etiqueta, color = colorDe(protocolo.estado))
                }
                if (protocolo.observaciones.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.observaciones_detalle, protocolo.observaciones),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 5) Foto de evidencia que tomó el técnico (el mismo componente del formulario)
            TarjetaSeccion(titulo = stringResource(R.string.seccion_foto)) {
                if (foto != null) {
                    FotoEvidencia(foto = foto, descripcion = stringResource(R.string.cd_foto_evidencia))
                } else {
                    Text(text = stringResource(R.string.sin_foto), style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Comparar con las pruebas anteriores del mismo equipo (lo pide el caso)
            BotonSecundario(
                texto = stringResource(R.string.btn_historial),
                onClick = onVerHistorial,
                modifier = Modifier.fillMaxWidth()
            )

            // Solo se puede formalizar si está "Registrado en terreno";
            // si ya está formalizado, el botón queda bloqueado (gris).
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

// MEDICIÓN: una fila "nombre ........ valor", como en un informe.
// SpaceBetween = el nombre a la izquierda y el valor a la derecha.
@Composable
private fun FilaMedicion(etiqueta: String, valor: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = etiqueta, style = MaterialTheme.typography.bodyMedium)
        Text(
            // 13.1 -> "13,1" (Formatos.kt: el mismo formato que usa el gráfico)
            text = formatearMedicion(valor),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// Protocolo de ejemplo para las vistas previas (TR-0142, recién registrado en terreno)
private val protocoloPreview = Protocolo(
    equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
    atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
    resultado = Resultado.CON_OBSERVACION,
    observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
    estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
    codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
    instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
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