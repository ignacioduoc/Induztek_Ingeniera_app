package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.induztek_ingeniera_app.model.DatoGrafico
import com.example.induztek_ingeniera_app.ui.formatearMedicion

/**
 * Gráfico de barras dibujado a mano con Canvas (el GraficoBarras del profe).
 * Idea clave: la barra más alta ocupa todo el alto disponible
 * y las demás se escalan con una regla de tres (valor / máximo).
 *
 * ANIMACIÓN: "progreso" va de 0 a 1 en 800 ms y cada barra se dibuja con
 * su alto x progreso: por eso las barras CRECEN desde el eje.
 * Se repite cada vez que cambian los datos (al elegir otra medición).
 */
@Composable
fun GraficoBarras(
    datos: List<DatoGrafico>,
    colores: List<Color>,
    modifier: Modifier = Modifier
) {
    val medidorTexto = rememberTextMeasurer()
    val colorTexto = MaterialTheme.colorScheme.onSurface
    val colorEje = MaterialTheme.colorScheme.outline

    // Animación: va de 0 a 1 cada vez que cambian los datos
    val progreso = remember(datos) { Animatable(0f) }
    LaunchedEffect(datos) {
        progreso.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (datos.isEmpty()) return@Canvas

        val margenSuperior = 20.dp.toPx()   // espacio para el número sobre la barra
        val margenInferior = 24.dp.toPx()   // espacio para la fecha bajo el eje
        val altoDisponible = size.height - margenSuperior - margenInferior
        val baseY = size.height - margenInferior

        // CAMBIO (Decimales): el máximo es Double. Nunca es 0 (las mediciones se
        // validan > 0), pero coerceAtLeast evita dividir por 0 por si acaso.
        val valorMaximo = datos.maxOf { it.valor }.coerceAtLeast(0.1)
        val anchoColumna = size.width / datos.size
        val anchoBarra = anchoColumna * 0.6f

        // Eje X
        drawLine(
            color = colorEje,
            start = Offset(0f, baseY),
            end = Offset(size.width, baseY),
            strokeWidth = 1.dp.toPx()
        )

        datos.forEachIndexed { indice, dato ->
            // CAMBIO (Decimales): la división se hace con Double y se pasa a Float para dibujar
            val altoBarra = altoDisponible * (dato.valor / valorMaximo).toFloat() * progreso.value
            val inicioColumna = indice * anchoColumna
            val x = inicioColumna + (anchoColumna - anchoBarra) / 2
            val y = baseY - altoBarra

            // Barra
            drawRoundRect(
                color = colores[indice % colores.size],
                topLeft = Offset(x, y),
                size = Size(anchoBarra, altoBarra),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            // Valor sobre la barra
            val textoValor = medidorTexto.measure(
                text = formatearMedicion(dato.valor),      // CAMBIO (Decimales): 13.6 -> "13,6"
                style = TextStyle(fontSize = 12.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoValor,
                topLeft = Offset(
                    x = x + (anchoBarra - textoValor.size.width) / 2,
                    y = y - textoValor.size.height - 2.dp.toPx()
                )
            )

            // Etiqueta bajo el eje (la fecha de la prueba)
            val textoEtiqueta = medidorTexto.measure(
                text = dato.etiqueta,
                style = TextStyle(fontSize = 12.sp, color = colorTexto)
            )
            drawText(
                textLayoutResult = textoEtiqueta,
                topLeft = Offset(
                    x = inicioColumna + (anchoColumna - textoEtiqueta.size.width) / 2,
                    y = baseY + 4.dp.toPx()
                )
            )
        }
    }
}