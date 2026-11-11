package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo


/** Lista desplegable genérica con error por campo. Sirve para empresa, pregunta de seguridad, etc. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ListaDesplegable(
    etiqueta: String,
    textoSeleccionado: String,
    opciones: List<T>,
    textoDe: (T) -> String,
    onElegir: (T) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    var abierta by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = abierta,
        onExpandedChange = { abierta = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = textoSeleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = abierta) },
            isError = error != null,
            supportingText = { if (error != null) Text(error) },
            shape = EstilosCampo.forma,
            colors = EstilosCampo.colores(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = abierta, onDismissRequest = { abierta = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onElegir(opcion)
                        abierta = false
                    }
                )
            }
        }
    }
}