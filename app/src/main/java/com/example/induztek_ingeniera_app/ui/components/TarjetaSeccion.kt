package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.ui.styles.EstilosTarjeta
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

// =====================================================================
//  TarjetaSeccion.kt  —  CONTENEDOR CARD CON TÍTULO + "SLOT"
//  ---------------------------------------------------------------------
//  Card = contenedor Material con fondo, esquinas redondeadas y sombra
//         (en la web: un <div class="tarjeta">).
//
//  Este componente recibe su contenido como PARÁMETRO:
//      contenido: @Composable ColumnScope.() -> Unit
//  Eso se llama "slot": quien lo usa decide qué va adentro:
//      TarjetaSeccion(titulo = "Datos") {
//          CampoTexto(...)
//          CampoTexto(...)
//      }
//  "ColumnScope" significa que lo de adentro se ordena en una Column.
// =====================================================================

@Composable
fun TarjetaSeccion(
    titulo: String,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EstilosTarjeta.forma,
        colors = EstilosTarjeta.colores(),
        elevation = EstilosTarjeta.elevacion()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico) // gap entre hijos
        ) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()   // <- aquí se dibuja lo que venga entre llaves
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaSeccionPreview() {
    InduztekTheme {
        TarjetaSeccion(titulo = "Equipo", modifier = Modifier.padding(16.dp)) {
            Text("TR-0142 · Transformador 500 kVA")
            Text("Planta Lampa · S/E-02")
        }
    }
}