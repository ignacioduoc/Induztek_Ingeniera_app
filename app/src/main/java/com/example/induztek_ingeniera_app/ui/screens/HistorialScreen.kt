package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.model.DatoGrafico
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.ui.components.EtiquetaEstado
import com.example.induztek_ingeniera_app.ui.components.GraficoBarras
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.components.colorDe
import com.example.induztek_ingeniera_app.ui.formatearFecha
import com.example.induztek_ingeniera_app.ui.formatearFechaCorta
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

// =====================================================================
//  HistorialScreen.kt  —  PANTALLA 6: HISTORIAL DEL EQUIPO
//  ---------------------------------------------------------------------
//  Lo pide el caso: "historial de pruebas anteriores del mismo equipo
//  para fines de comparación". Tiene 3 partes:
//     1) Equipo
//     2) Gráfico de barras ANIMADO (el GraficoBarras del profe) con la
//        medición elegida en una lista desplegable
//     3) La lista de todas las pruebas del equipo, con sus chips
//  LazyColumn:  item { } = un elemento suelto (las tarjetas de arriba)
//               items(lista) { } = uno por cada prueba
//  No tiene ViewModel propio: recibe el equipo y su historial por parámetro.
// =====================================================================

// MEDICIÓN: las 4 mediciones que se pueden graficar.
// "nombre" es el id del texto en strings.xml (R.string.campo_at_bt...): se muestra
// con stringResource(medicion.nombre), los mismos nombres del formulario.
private enum class Medicion(val nombre: Int) {
    AT_BT(R.string.campo_at_bt),
    AT_TIERRA(R.string.campo_at_tierra),
    BT_TIERRA(R.string.campo_bt_tierra),
    INDICE_POLARIZACION(R.string.campo_indice_pol)
}

// MEDICIÓN: el valor de la medición elegida dentro de un protocolo
private fun valorDe(protocolo: Protocolo, medicion: Medicion): Double = when (medicion) {
    Medicion.AT_BT -> protocolo.atBt
    Medicion.AT_TIERRA -> protocolo.atTierra
    Medicion.BT_TIERRA -> protocolo.btTierra
    Medicion.INDICE_POLARIZACION -> protocolo.indicePolarizacion
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    equipo: Equipo,
    historial: List<Protocolo>,
    onVolver: () -> Unit
) {
    // Estado de la pantalla: qué medición se grafica y si la lista está abierta
    var medicion by remember { mutableStateOf(Medicion.AT_BT) }
    var expandido by remember { mutableStateOf(false) }

    // El historial llega del más nuevo al más antiguo. Para el gráfico se toman
    // las 6 más nuevas y se dan vuelta: la más antigua queda a la izquierda.
    val ultimas = historial.take(6).reversed()
    val datos = ultimas.map { protocolo ->
        DatoGrafico(etiqueta = formatearFechaCorta(protocolo.fechaHora), valor = valorDe(protocolo, medicion))
    }
    // Color de cada barra = su resultado (los mismos colores de los chips)
    val colores = ultimas.map { protocolo -> colorDe(protocolo.resultado) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_historial)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            // 1) Equipo
            item {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_equipo)) {
                    Text(text = "${equipo.codigo} · ${equipo.tipo}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = equipo.empresaCliente, style = MaterialTheme.typography.bodyMedium)
                    Text(text = equipo.instalacion, style = MaterialTheme.typography.bodyMedium)
                }
            }

            // 2) Gráfico de comparación
            item {
                TarjetaSeccion(titulo = stringResource(R.string.seccion_grafico)) {
                    // Sin pruebas no hay nada que graficar (igual que "sin_datos" del profe)
                    if (historial.isEmpty()) {
                        Text(text = stringResource(R.string.sin_pruebas), style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Lista desplegable para elegir la medición (igual que "Tipo de prueba")
                        ExposedDropdownMenuBox(
                            expanded = expandido,
                            onExpandedChange = { expandido = it }
                        ) {
                            OutlinedTextField(
                                value = stringResource(medicion.nombre),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.campo_medicion)) },
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
                                Medicion.entries.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(opcion.nombre)) },
                                        onClick = {
                                            medicion = opcion        // cambian los datos -> el gráfico se anima otra vez
                                            expandido = false
                                        }
                                    )
                                }
                            }
                        }

                        // Las barras crecen desde 0 hasta su valor (animación del profe)
                        GraficoBarras(datos = datos, colores = colores)

                        Text(
                            text = stringResource(R.string.leyenda_grafico),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 3) Todas las pruebas del equipo, de la más nueva a la más antigua
            if (historial.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.seccion_pruebas),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            items(historial, key = { it.id }) { protocolo ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        overlineContent = { Text(formatearFecha(protocolo.fechaHora)) },
                        headlineContent = { Text(protocolo.tipoPrueba) },
                        supportingContent = {
                            Column(verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                Text(stringResource(R.string.tecnico_nombre, protocolo.nombreTecnico))
                                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                                    EtiquetaEstado(texto = protocolo.resultado.etiqueta, color = colorDe(protocolo.resultado))
                                    EtiquetaEstado(texto = protocolo.estado.etiqueta, color = colorDe(protocolo.estado))
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// Datos de ejemplo para la vista previa: TR-0142 con sus 3 pruebas de la BD
// y una nueva con observación (de la más nueva a la más antigua, como en la BD)
private val equipoPreview = Equipo(
    "TR-0142", "Transformador 500 kVA", "Planta Lampa · S/E-02",
    clienteId = 2, id = 1, empresaCliente = "Industrias Aurora S.A."
)

private val historialPreview = listOf(
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1792503000000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 12.9, atTierra = 9.1, btTierra = 6.2, indicePolarizacion = 1.8,
        resultado = Resultado.CON_OBSERVACION,
        observaciones = "Índice de polarización bajo 2,0: repetir la prueba en 3 meses.",
        estado = EstadoProtocolo.REGISTRADO_EN_TERRENO, id = 5,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
    ),
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1776174300000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 13.1, atTierra = 9.4, btTierra = 7.8, indicePolarizacion = 2.4,
        resultado = Resultado.APROBADO, observaciones = "", estado = EstadoProtocolo.FORMALIZADO, id = 3,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
    ),
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1760019300000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 13.4, atTierra = 9.6, btTierra = 8.1, indicePolarizacion = 2.4,
        resultado = Resultado.APROBADO, observaciones = "", estado = EstadoProtocolo.FORMALIZADO, id = 2,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
    ),
    Protocolo(
        equipoId = 1, tecnicoId = 1, fechaHora = 1744727400000, tipoPrueba = "Aislamiento 5 kV DC",
        atBt = 13.6, atTierra = 9.8, btTierra = 8.3, indicePolarizacion = 2.5,
        resultado = Resultado.APROBADO, observaciones = "", estado = EstadoProtocolo.FORMALIZADO, id = 1,
        codigoEquipo = "TR-0142", tipoEquipo = "Transformador 500 kVA",
        instalacion = "Planta Lampa · S/E-02", nombreTecnico = "Matías Rojas"
    )
)

// Ojo: en la vista previa normal las barras salen en 0 porque la animación no corre.
// Para verlas crecer: botón "Start Interactive Mode" de la vista previa, o ejecutar la app.
@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun HistorialPreview() {
    InduztekTheme {
        HistorialScreen(equipo = equipoPreview, historial = historialPreview, onVolver = {})
    }
}

@Preview(showBackground = true, name = "Equipo sin pruebas")
@Composable
private fun HistorialVacioPreview() {
    InduztekTheme {
        HistorialScreen(
            equipo = equipoPreview.copy(codigo = "TR-0215", tipo = "Transformador 1000 kVA",
                instalacion = "Planta Quilicura · S/E-03", id = 3),
            historial = emptyList(),
            onVolver = {}
        )
    }
}