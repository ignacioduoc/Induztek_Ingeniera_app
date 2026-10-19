package com.example.induztek_ingeniera_app.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.ui.theme.Blanco
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.GrisDeshabilitado
import com.example.induztek_ingeniera_app.ui.theme.Primario

// =====================================================================
//  EstilosBoton.kt  —  "HOJA DE ESTILOS" DE LOS BOTONES
//  ---------------------------------------------------------------------
//  Compose NO usa CSS, pero se logra lo mismo agrupando estilos:
//
//     CSS                              Compose
//     -------------------------------  ---------------------------------
//     .btn-principal { ... }           object EstilosBoton { ... }
//     background-color / color         ButtonColors
//     border-radius                    Shape (RoundedCornerShape)
//     border: 2px solid                BorderStroke(2.dp, color)
//     padding                          PaddingValues
//     box-shadow                       ButtonElevation
//     height: 52px                     Modifier.height(52.dp)
// =====================================================================

object EstilosBoton {

    val forma: Shape = RoundedCornerShape(Dimens.radioBoton)              // border-radius
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp)      // padding

    /** Botón principal: relleno verde, texto blanco. */
    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Primario,             // background-color
        contentColor = Blanco,                 // color
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )

    /** Botón secundario: transparente, texto del color primario del tema. */
    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )

    /** Borde del botón secundario. */
    @Composable
    fun bordeSecundario(): BorderStroke =
        BorderStroke(Dimens.bordeBoton, MaterialTheme.colorScheme.primary)

    /** Sombra del botón principal (al presionar "se hunde"). */
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = 1.dp
    )
}

// "Clase CSS" como extensión de Modifier: .alto-boton { height: 52px; }
// Se encadena con otros modificadores: Modifier.weight(1f).estiloAltoBoton()
fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)