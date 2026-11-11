package com.example.induztek_ingeniera_app.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.ui.components.BarraBusquedaFiltro
import com.example.induztek_ingeniera_app.ui.components.FilaDato
import com.example.induztek_ingeniera_app.ui.components.GrupoFiltro
import com.example.induztek_ingeniera_app.ui.components.coincideBusqueda
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme


/**
 * Empresas en modo consulta (sin crear ni editar).
 * El técnico ve todas ("Empresas"); el cliente solo la suya ("Mi empresa").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpresasConsultaScreen(
    titulo: String,
    indicacion: String,
    empresas: List<EmpresaEntity>,
    onVolver: () -> Unit
) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var comunaElegida by rememberSaveable { mutableStateOf<String?>(null) }

    val comunas = empresas.mapNotNull { comunaDe(it.direccion) }.distinct().sorted()
    val empresasFiltradas = empresas.filter { e ->
        (comunaElegida == null || comunaDe(e.direccion) == comunaElegida) &&
                (coincideBusqueda(e.nombre, consulta) || coincideBusqueda(e.direccion, consulta))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // Con una sola empresa (cliente) no hace falta buscar ni filtrar
            if (empresas.size > 1) {
                BarraBusquedaFiltro(
                    consulta = consulta,
                    onConsultaCambia = { consulta = it },
                    placeholder = stringResource(R.string.buscar_empresas),
                    grupos = listOf(
                        GrupoFiltro(
                            titulo = stringResource(R.string.filtro_comuna),
                            opciones = comunas,
                            seleccionada = comunaElegida,
                            onElegir = { comunaElegida = it }
                        )
                    ),
                    modifier = Modifier.padding(
                        start = Dimens.espacioMedio, end = Dimens.espacioMedio, top = Dimens.espacioChico
                    )
                )
            }

            Text(
                text = indicacion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
            )

            if (empresas.isEmpty() || empresasFiltradas.isEmpty()) {
                Text(
                    text = stringResource(
                        if (empresas.isEmpty()) R.string.lista_vacia_empresas else R.string.sin_resultados
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(Dimens.espacioMedio)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.espacioMedio, end = Dimens.espacioMedio,
                    top = Dimens.espacioChico, bottom = Dimens.espacioMedio
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
            ) {
                items(empresasFiltradas, key = { it.idEmpresa }) { empresa ->
                    TarjetaEmpresaConsulta(empresa)
                }
            }
        }
    }
}


// La comuna es lo que va después de la última coma: "Av. Providencia 1234, Providencia" -> "Providencia"
private fun comunaDe(direccion: String): String? =
    direccion.substringAfterLast(",", "").trim().ifBlank { null }


@Composable
private fun TarjetaEmpresaConsulta(empresa: EmpresaEntity) {
    val bitmapLogo = remember(empresa.logo) {
        empresa.logo?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.espacioMedio),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (bitmapLogo != null) {
                    Image(
                        bitmap = bitmapLogo.asImageBitmap(),
                        contentDescription = stringResource(R.string.cd_logo_empresa),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.espacioMini)
            ) {
                Text(
                    text = empresa.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                FilaDato(
                    icono = Icons.Default.LocationOn,
                    etiqueta = stringResource(R.string.etiqueta_direccion_tarjeta),
                    valor = empresa.direccion.ifBlank { stringResource(R.string.sin_direccion_registrada) }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun EmpresasConsultaPreview() {
    InduztekTheme {
        EmpresasConsultaScreen(
            titulo = "Empresas",
            indicacion = "Empresas registradas en el sistema.",
            empresas = listOf(
                EmpresaEntity(1, "Induztek Ingeniería SpA", "Av. Providencia 1234, Providencia"),
                EmpresaEntity(2, "Industrias Aurora S.A.", "Camino Los Maitenes 1200, Lampa")
            ),
            onVolver = {}
        )
    }
}