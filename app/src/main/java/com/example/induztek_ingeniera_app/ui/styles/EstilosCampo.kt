package com.example.induztek_ingeniera_app.ui.styles

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.GrisDeshabilitado

// =====================================================================
//  EstilosCampo.kt  —  "HOJA DE ESTILOS" DE LOS CAMPOS DEL FORMULARIO
//  ---------------------------------------------------------------------
//  Equivale en CSS a:
//     input            { border-radius: 12px; border-color: gris; }
//     input:focus      { border-color: verde; }
//     input.error      { border-color: rojo; }
//  En Compose cada "estado" (enfocado, sin foco, con error) tiene su color.
// =====================================================================

object EstilosCampo {

    val forma: Shape = RoundedCornerShape(Dimens.radioCampo)

    @Composable
    fun colores(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,      // input:focus
        unfocusedBorderColor = GrisDeshabilitado,                    // input normal
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        errorBorderColor = MaterialTheme.colorScheme.error,          // input.error
        cursorColor = MaterialTheme.colorScheme.primary
    )
}