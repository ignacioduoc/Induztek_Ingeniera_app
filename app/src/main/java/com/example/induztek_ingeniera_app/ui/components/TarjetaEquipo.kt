package com.example.induztek_ingeniera_app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity

@Composable
fun TarjetaEquipo(
    equipo: EquipoEntity,
    onVerMapaClick: (lat: Double, lng: Double, codigo: String, instalacion: String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = equipo.codigo, style = MaterialTheme.typography.titleMedium)
                Text(text = equipo.tipo, style = MaterialTheme.typography.bodyMedium)
                Text(text = equipo.instalacion, style = MaterialTheme.typography.bodySmall)
                if (equipo.direccion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 ${equipo.direccion}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }


            IconButton(
                onClick = {
                    onVerMapaClick(equipo.latitud, equipo.longitud, equipo.codigo, equipo.instalacion)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu), // Puedes sustituir por R.drawable.ic_location cuando agregues el SVG
                    contentDescription = "Ver ubicación en el mapa",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}