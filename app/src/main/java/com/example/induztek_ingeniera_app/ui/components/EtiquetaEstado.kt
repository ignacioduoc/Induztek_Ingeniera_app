package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.model.EstadoProtocolo
import com.example.induztek_ingeniera_app.model.Resultado
import com.example.induztek_ingeniera_app.ui.theme.Acento
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.Exito
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.theme.Secundario

// =====================================================================
//  EtiquetaEstado.kt  —  "Chip" de color para el resultado y el estado.
//  ---------------------------------------------------------------------
//  Adaptado de EtiquetaInteres del profe: la misma píldora, pero
//   - el COLOR llega por parámetro (cada resultado / estado tiene el suyo)
//   - es más compacta (letra labelMedium y menos relleno) para que quepan
//     DOS chips en una fila de la lista.
//  Surface = el contenedor más simple de Material: pinta un fondo con
//  forma y color, y pone su contenido encima.
//  En CSS: <span class="chip"> con background, border-radius:50px y padding.
// =====================================================================

@Composable
fun EtiquetaEstado(
    texto: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),                // 50 % = píldora
        color = color.copy(alpha = 0.15f),             // fondo: el mismo color, suave (15 %)
        contentColor = color                           // texto: el color fuerte
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = Dimens.espacioChico, vertical = Dimens.espacioMini)
        )
    }
}

// Color de cada RESULTADO (ver los comentarios de Color.kt).
// Dos funciones con el mismo nombre y distinto tipo de parámetro = SOBRECARGA:
// Kotlin elige la correcta según lo que reciba.
@Composable
fun colorDe(resultado: Resultado): Color = when (resultado) {
    Resultado.APROBADO -> Exito                                // verde
    Resultado.CON_OBSERVACION -> Acento                        // ámbar
    Resultado.RECHAZADO -> MaterialTheme.colorScheme.error     // rojo (del tema)
}

// Color de cada ESTADO del protocolo
@Composable
fun colorDe(estado: EstadoProtocolo): Color = when (estado) {
    EstadoProtocolo.REGISTRADO_EN_TERRENO -> Secundario        // azul
    EstadoProtocolo.FORMALIZADO -> Exito                       // verde
}

@Preview(showBackground = true)
@Composable
private fun EtiquetaEstadoPreview() {
    InduztekTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Los 3 resultados: Aprobado · Con observación · Rechazado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Resultado.entries.forEach { EtiquetaEstado(texto = it.etiqueta, color = colorDe(it)) }
            }
            // Los 2 estados: Registrado en terreno · Formalizado
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstadoProtocolo.entries.forEach { EtiquetaEstado(texto = it.etiqueta, color = colorDe(it)) }
            }
        }
    }
}