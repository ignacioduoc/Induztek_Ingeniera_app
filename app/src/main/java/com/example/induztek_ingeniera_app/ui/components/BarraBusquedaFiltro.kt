package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import java.text.Normalizer


/**
 * Un grupo de opciones del botón de filtrar (ej: "Rol" con Administrador / Técnico / Cliente).
 * seleccionada = null significa "sin filtro" (se muestra todo).
 */
data class GrupoFiltro(
    val titulo: String,
    val opciones: List<String>,
    val seleccionada: String?,
    val onElegir: (String?) -> Unit
)


/** Busca ignorando mayúsculas y tildes: "tecnico" encuentra "Técnico". */
fun coincideBusqueda(texto: String, consulta: String): Boolean {
    if (consulta.isBlank()) return true
    return texto.sinTildes().contains(consulta.trim().sinTildes(), ignoreCase = true)
}

private fun String.sinTildes(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")


/**
 * Barra de búsqueda + botón de filtrar, para poner arriba de las listas del administrador.
 * Debajo se muestran los filtros activos como chips que se pueden quitar con la X.
 */
@Composable
fun BarraBusquedaFiltro(
    consulta: String,
    onConsultaCambia: (String) -> Unit,
    placeholder: String,
    grupos: List<GrupoFiltro>,
    modifier: Modifier = Modifier
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val hayFiltroActivo = grupos.any { it.seleccionada != null }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
        ) {

            // ---- Campo de búsqueda (borde redondeado, ícono de lupa, X para borrar) ----
            OutlinedTextField(
                value = consulta,
                onValueChange = onConsultaCambia,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (consulta.isNotEmpty()) {
                        IconButton(onClick = { onConsultaCambia("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_limpiar_busqueda)
                            )
                        }
                    }
                },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            // ---- Botón de filtrar (se pinta del color primario cuando hay un filtro activo) ----
            Box {
                Surface(
                    onClick = { menuAbierto = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (hayFiltroActivo) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface,
                    contentColor = if (hayFiltroActivo) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.primary,
                    border = BorderStroke(
                        1.dp,
                        if (hayFiltroActivo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterAlt,
                            contentDescription = stringResource(R.string.cd_filtrar)
                        )
                    }
                }

                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    grupos.forEachIndexed { indice, grupo ->
                        if (indice > 0) HorizontalDivider()

                        Text(
                            text = grupo.titulo,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = Dimens.espacioMedio, vertical = Dimens.espacioChico)
                        )
                        OpcionFiltro(
                            texto = stringResource(R.string.filtro_todos),
                            elegida = grupo.seleccionada == null,
                            onClick = { grupo.onElegir(null); menuAbierto = false }
                        )
                        grupo.opciones.forEach { opcion ->
                            OpcionFiltro(
                                texto = opcion,
                                elegida = grupo.seleccionada == opcion,
                                onClick = { grupo.onElegir(opcion); menuAbierto = false }
                            )
                        }
                    }

                    if (hayFiltroActivo) {
                        HorizontalDivider()
                        TextButton(
                            onClick = { grupos.forEach { it.onElegir(null) }; menuAbierto = false },
                            modifier = Modifier.padding(horizontal = Dimens.espacioChico)
                        ) { Text(stringResource(R.string.filtro_limpiar)) }
                    }
                }
            }
        }

        // ---- Chips con los filtros activos ----
        if (hayFiltroActivo) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                grupos.filter { it.seleccionada != null }.forEach { grupo ->
                    InputChip(
                        selected = true,
                        onClick = { grupo.onElegir(null) },
                        label = { Text("${grupo.titulo}: ${grupo.seleccionada}") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_quitar_filtro),
                                modifier = Modifier.size(InputChipDefaults.IconSize)
                            )
                        },
                        shape = CircleShape
                    )
                }
            }
        }
    }
}


@Composable
private fun OpcionFiltro(texto: String, elegida: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = {
            Text(
                text = texto,
                color = if (elegida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (elegida) FontWeight.SemiBold else FontWeight.Normal
            )
        },
        trailingIcon = {
            if (elegida) {
                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        },
        onClick = onClick
    )
}


@Preview(showBackground = true)
@Composable
private fun BarraBusquedaFiltroPreview() {
    InduztekTheme {
        Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
            BarraBusquedaFiltro(
                consulta = "si",
                onConsultaCambia = {},
                placeholder = "Buscar usuario",
                grupos = listOf(
                    GrupoFiltro("Rol", listOf("Administrador", "Técnico", "Cliente"), "Cliente") {}
                )
            )
        }
    }
}