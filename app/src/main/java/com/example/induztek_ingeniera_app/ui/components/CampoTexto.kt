package com.example.induztek_ingeniera_app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme


@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    // CAMBIO (Ícono opcional): igual que el ícono de BotonPrincipal del profe
    @DrawableRes icono: Int? = null,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = { Text(etiqueta) },                       // texto flotante (como <label>)
        leadingIcon = if (icono != null) {
            {                                             // ícono a la izquierda
                Icon(
                    painter = painterResource(id = icono),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            }
        } else {
            null
        },

        trailingIcon = {                                  // ícono a la DERECHA
            if (error != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            }
        },
        isError = error != null,
        supportingText = { if (error != null) Text(error) }, // texto de ayuda bajo el campo
        singleLine = true,                                // una sola línea
        keyboardOptions = KeyboardOptions(
            keyboardType = tipoTeclado,
            imeAction = ImeAction.Next                    // botón "Siguiente" en el teclado
        ),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview() {
    InduztekTheme {
        CampoTexto(
            valor = "tecnico@",
            onValorCambia = {},
            etiqueta = "Correo electrónico",
            icono = R.drawable.ic_correo,
            error = "Ingresa un correo válido",
            modifier = Modifier.padding(16.dp)
        )
    }
}