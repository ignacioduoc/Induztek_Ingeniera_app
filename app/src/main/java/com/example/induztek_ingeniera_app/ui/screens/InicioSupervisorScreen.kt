package com.example.induztek_ingeniera_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.data.entities.EquipoConDetalles
import com.example.induztek_ingeniera_app.model.Usuario
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.viewmodel.SupervisorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioSupervisorScreen(
    usuario: Usuario?,
    viewModel: SupervisorViewModel,
    onIrGestionUsuarios: () -> Unit = {},
    onEditarPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val equipos by viewModel.equipos.collectAsStateWithLifecycle()
    val tecnicos by viewModel.tecnicos.collectAsStateWithLifecycle()
    val formState by viewModel.nuevoTecnicoState.collectAsStateWithLifecycle()

    var tabSeleccionada by remember { mutableIntStateOf(0) }
    var equipoAAsignar by remember { mutableStateOf<EquipoConDetalles?>(null) }
    var mostrarDialogoNuevoTecnico by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Supervisor", style = MaterialTheme.typography.titleMedium)
                        usuario?.let {
                            Text(
                                text = "${it.nombre} ${it.apellido}".trim(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onIrGestionUsuarios) { Text("Usuarios") }
                    TextButton(onClick = onEditarPerfil) { Text("Perfil") }
                    TextButton(onClick = onCerrarSesion) { Text("Salir") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = tabSeleccionada) {
                Tab(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    text = { Text("Solicitudes (${equipos.size})") }
                )
                Tab(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    text = { Text("Técnicos (${tecnicos.size})") }
                )
            }

            if (tabSeleccionada == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    items(equipos, key = { it.equipo.id_equipo }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimens.espacioMedio),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${item.equipo.codigo} · ${item.equipo.tipo}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instalación: ${item.equipo.instalacion}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Cliente: ${item.nombreCliente.ifBlank { "Sin cliente" }} (${item.nombreEmpresa.ifBlank { "Particular" }})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = if (item.nombreTecnico.isNotBlank())
                                        "Técnico asignado: ${item.nombreTecnico}"
                                    else
                                        "Estado: Pendiente de asignar técnico",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (item.nombreTecnico.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error
                                )
                                OutlinedButton(
                                    onClick = { equipoAAsignar = item },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (item.nombreTecnico.isNotBlank()) "Reasignar técnico" else "Asignar técnico")
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.espacioPantalla),
                    verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
                ) {
                    BotonPrincipal(
                        texto = "Gestionar cuentas (Técnicos y Clientes)",
                        onClick = onIrGestionUsuarios,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedButton(
                        onClick = {
                            viewModel.limpiarFormularioTecnico()
                            mostrarDialogoNuevoTecnico = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Crear cuenta rápida de técnico")
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
                    ) {
                        items(tecnicos, key = { it.idUsuario }) { tec ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(Dimens.espacioMedio)) {
                                    Text(
                                        text = "${tec.nombre} ${tec.apellido}".trim(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Correo: ${tec.correo}", style = MaterialTheme.typography.bodyMedium)
                                    Text("RUT: ${tec.rut ?: "N/A"} · Tel: ${tec.telefono.ifBlank { "N/A" }}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para asignar/reasignar técnico a una solicitud de equipo
    equipoAAsignar?.let { item ->
        var tecnicoSeleccionadoId by remember(item) { mutableStateOf(item.equipo.id_tecnico) }

        AlertDialog(
            onDismissRequest = { equipoAAsignar = null },
            title = { Text("Asignar técnico a ${item.equipo.codigo}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tecnicoSeleccionadoId == null,
                            onClick = { tecnicoSeleccionadoId = null }
                        )
                        Text("Sin asignar")
                    }
                    tecnicos.forEach { tec ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tecnicoSeleccionadoId == tec.idUsuario,
                                onClick = { tecnicoSeleccionadoId = tec.idUsuario }
                            )
                            Text("${tec.nombre} ${tec.apellido}".trim())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.asignarTecnico(item.equipo.id_equipo, tecnicoSeleccionadoId)
                    equipoAAsignar = null
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { equipoAAsignar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo para crear nueva cuenta de técnico
    if (mostrarDialogoNuevoTecnico) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoNuevoTecnico = false },
            title = { Text("Nueva cuenta de Técnico") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CampoTexto(
                        valor = formState.nombre,
                        onValorCambia = viewModel::onNombreCambia,
                        etiqueta = "Nombre",
                        error = formState.errorNombre
                    )
                    CampoTexto(
                        valor = formState.apellido,
                        onValorCambia = viewModel::onApellidoCambia,
                        etiqueta = "Apellido",
                        error = formState.errorApellido
                    )
                    CampoTexto(
                        valor = formState.rut,
                        onValorCambia = viewModel::onRutCambia,
                        etiqueta = "RUT (ej. 12.345.678-9)",
                        error = formState.errorRut
                    )
                    CampoTexto(
                        valor = formState.telefono,
                        onValorCambia = viewModel::onTelefonoCambia,
                        etiqueta = "Teléfono (Ej: 912345678)",
                        error = formState.errorTelefono,
                        tipoTeclado = KeyboardType.Phone
                    )
                    CampoTexto(
                        valor = formState.correo,
                        onValorCambia = viewModel::onCorreoCambia,
                        etiqueta = "Correo electrónico",
                        error = formState.errorCorreo,
                        tipoTeclado = KeyboardType.Email
                    )
                    CampoTexto(
                        valor = formState.contrasena,
                        onValorCambia = viewModel::onContrasenaCambia,
                        etiqueta = "Contraseña temporal (mín. 6)",
                        error = formState.errorContrasena
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.crearCuentaTecnico {
                        mostrarDialogoNuevoTecnico = false
                    }
                }) {
                    Text("Crear técnico")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoNuevoTecnico = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}