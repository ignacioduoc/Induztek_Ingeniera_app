package com.example.induztek_ingeniera_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.data.entities.EmpresaEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.GrupoRadio
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.styles.EstilosCampo
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.ui.utils.buscarCoordenadasGratis


// Formatos válidos para el código, la planta y el nombre del tipo
private val formatoCodigo = Regex("^[A-Za-z]{2,4}-\\d{3,5}$")   // Ej: TR-0142
private val formatoPlanta = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 ]{3,40}$")   // Ej: Lampa, Puerto Montt
private val formatoNombreTipo = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{3,30}$")   // Ej: Motor, Interruptor

// Tipos que se ofrecen en la lista y unidades para los "otros" tipos
private const val TIPO_TRANSFORMADOR = "Transformador"
private const val TIPO_OTRO = "OTRO"
private val unidadesTipo = listOf("kVA", "kW", "kV", "A", "HP")

// "puerto MONTT" -> "Puerto Montt" (cada palabra con solo la primera letra en mayúscula)
private fun String.conMayusculaInicial(): String =
    trim().split(" ").filter { it.isNotBlank() }
        .joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }


// Tarea 4: pantalla para que el técnico agregue un equipo en la empresa que visitó
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEquipoScreen(
    equipos: List<Equipo>,
    empresas: List<EmpresaEntity>,
    clientes: List<UsuarioEntity>,
    onGuardar: (
        codigo: String, tipo: String, instalacion: String,
        idEmpresa: Long, idCliente: Long,
        direccion: String, latitud: Double, longitud: Double,
        onResultado: (Boolean) -> Unit
    ) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var codigo by rememberSaveable { mutableStateOf("") }
    var tipoElegido by rememberSaveable { mutableStateOf("") }      // "" / Transformador / OTRO
    var nombreTipo by rememberSaveable { mutableStateOf("") }       // solo si elige "Otro tipo"
    var valorTipo by rememberSaveable { mutableStateOf("") }        // potencia o valor (solo número)
    var unidadTipo by rememberSaveable { mutableStateOf("kVA") }    // solo si elige "Otro tipo"
    var idEmpresa by rememberSaveable { mutableStateOf(0L) }   // 0 = todavía no elige
    var idCliente by rememberSaveable { mutableStateOf(0L) }
    var instalacion by rememberSaveable { mutableStateOf("") }          // la elegida de la lista
    var nombrePlanta by rememberSaveable { mutableStateOf("") }         // si es una instalación nueva
    var numeroSE by rememberSaveable { mutableStateOf("") }             // si es una instalación nueva
    var escribirOtra by rememberSaveable { mutableStateOf(false) }   // true = el técnico escribe una instalación nueva
    var intentoGuardar by rememberSaveable { mutableStateOf(false) } // los errores aparecen recién al apretar Guardar
    var codigoRepetido by rememberSaveable { mutableStateOf(false) }
    var guardando by rememberSaveable { mutableStateOf(false) }
    var listaAbierta by remember { mutableStateOf(false) }
    var listaTipoAbierta by remember { mutableStateOf(false) }

    // Solo se ofrecen empresas que tengan al menos un cliente (el equipo necesita dueño)
    val empresasConClientes = empresas.filter { e -> clientes.any { it.idEmpresa == e.idEmpresa } }
    val clientesDeLaEmpresa = clientes.filter { it.idEmpresa == idEmpresa }

    // La dirección del equipo es la de la empresa elegida (se coloca sola)
    val direccionEmpresa = empresasConClientes.firstOrNull { it.idEmpresa == idEmpresa }?.direccion.orEmpty()

    // Instalaciones que ya existen en esa empresa (salen de sus equipos)
    val instalacionesDisponibles = equipos
        .filter { it.idEmpresa == idEmpresa && it.instalacion.isNotBlank() }
        .map { it.instalacion }
        .distinct()
        .sorted()
    val escribirNueva = escribirOtra || instalacionesDisponibles.isEmpty()

    // Tipo de equipo: de la lista (Transformador) o uno nuevo. La unidad se pone sola, no se escribe
    val esTransformador = tipoElegido == TIPO_TRANSFORMADOR
    val esOtroTipo = tipoElegido == TIPO_OTRO
    val nombreTipoValido = formatoNombreTipo.matches(nombreTipo.trim())
    val valorTipoValido = valorTipo.toIntOrNull()?.let { it in 1..99999 } == true
    val tipoFinal = when {
        esTransformador && valorTipoValido -> "$TIPO_TRANSFORMADOR ${valorTipo.toInt()} kVA"
        esOtroTipo && nombreTipoValido && valorTipoValido -> "${nombreTipo.conMayusculaInicial()} ${valorTipo.toInt()} $unidadTipo"
        else -> ""
    }

    // Instalación nueva = nombre de la planta + número de subestación (S/E)
    val plantaValida = formatoPlanta.matches(nombrePlanta.trim())
    val numeroSEValido = numeroSE.toIntOrNull()?.let { it in 1..99 } == true

    // Instalación final: la de la lista, o la armada con planta y S/E (ej: "Planta Lampa · S/E-02")
    val instalacionFinal = when {
        !escribirNueva -> instalacion.trim()
        plantaValida && numeroSEValido -> "Planta ${nombrePlanta.conMayusculaInicial()} · S/E-${numeroSE.toInt().toString().padStart(2, '0')}"
        else -> ""
    }

    val errorCodigo = when {
        codigoRepetido -> stringResource(R.string.error_codigo_repetido)
        intentoGuardar && codigo.isBlank() -> stringResource(R.string.error_codigo_vacio)
        intentoGuardar && !formatoCodigo.matches(codigo.trim()) -> stringResource(R.string.error_codigo_formato)
        else -> null
    }
    val errorTipo = if (intentoGuardar && tipoElegido.isBlank()) stringResource(R.string.error_elegir_tipo) else null
    val errorNombreTipo = if (intentoGuardar && esOtroTipo) {
        when {
            nombreTipo.isBlank() -> stringResource(R.string.error_nombre_tipo_vacio)
            !nombreTipoValido -> stringResource(R.string.error_nombre_tipo_formato)
            else -> null
        }
    } else null
    val errorValorTipo = if (intentoGuardar && tipoElegido.isNotBlank() && !valorTipoValido) stringResource(R.string.error_valor_tipo) else null
    val errorEmpresa = if (intentoGuardar && idEmpresa == 0L) stringResource(R.string.error_elegir_empresa) else null
    val errorCliente = if (intentoGuardar && idEmpresa != 0L && idCliente == 0L) stringResource(R.string.error_elegir_cliente) else null
    val errorInstalacion = if (intentoGuardar && idEmpresa != 0L && !escribirNueva && instalacion.isBlank()) stringResource(R.string.error_elegir_instalacion) else null
    val errorPlanta = if (intentoGuardar && idEmpresa != 0L && escribirNueva) {
        when {
            nombrePlanta.isBlank() -> stringResource(R.string.error_planta_vacia)
            !plantaValida -> stringResource(R.string.error_planta_formato)
            else -> null
        }
    } else null
    val errorNumeroSE = if (intentoGuardar && idEmpresa != 0L && escribirNueva && !numeroSEValido) stringResource(R.string.error_numero_se) else null

    fun guardarEquipo() {
        intentoGuardar = true
        val todoOk = formatoCodigo.matches(codigo.trim()) && tipoFinal.isNotBlank() &&
                idEmpresa != 0L && idCliente != 0L && instalacionFinal.isNotBlank()
        if (!todoOk) return

        guardando = true
        val guardar: (Double, Double) -> Unit = { lat, lng ->
            onGuardar(
                codigo.trim().uppercase(), tipoFinal, instalacionFinal,
                idEmpresa, idCliente, direccionEmpresa, lat, lng
            ) { guardado ->
                guardando = false
                if (guardado) {
                    Toast.makeText(context, context.getString(R.string.equipo_agregado), Toast.LENGTH_SHORT).show()
                    onVolver()
                } else {
                    codigoRepetido = true
                }
            }
        }
        // Con la dirección de la empresa se buscan las coordenadas para el mapa
        if (direccionEmpresa.isNotBlank()) {
            buscarCoordenadasGratis(context, direccionEmpresa) { punto ->
                guardar(punto?.latitude ?: 0.0, punto?.longitude ?: 0.0)
            }
        } else {
            guardar(0.0, 0.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_agregar_equipo)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.espacioPantalla),
            verticalArrangement = Arrangement.spacedBy(Dimens.espacioMedio)
        ) {

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_equipo)) {
                Text(
                    text = stringResource(R.string.indicacion_formato_equipo),
                    style = MaterialTheme.typography.bodySmall
                )
                CampoTexto(
                    valor = codigo,
                    onValorCambia = { codigo = it; codigoRepetido = false },
                    etiqueta = stringResource(R.string.campo_codigo_equipo),
                    error = errorCodigo
                )
                // Lista desplegable de tipos de equipo
                ExposedDropdownMenuBox(
                    expanded = listaTipoAbierta,
                    onExpandedChange = { listaTipoAbierta = it }
                ) {
                    OutlinedTextField(
                        value = when (tipoElegido) {
                            TIPO_TRANSFORMADOR -> TIPO_TRANSFORMADOR
                            TIPO_OTRO -> stringResource(R.string.opcion_otro_tipo)
                            else -> ""
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.campo_tipo_equipo)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = listaTipoAbierta) },
                        isError = errorTipo != null,
                        supportingText = { if (errorTipo != null) Text(errorTipo) },
                        shape = EstilosCampo.forma,
                        colors = EstilosCampo.colores(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = listaTipoAbierta,
                        onDismissRequest = { listaTipoAbierta = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(TIPO_TRANSFORMADOR) },
                            onClick = { tipoElegido = TIPO_TRANSFORMADOR; nombreTipo = ""; valorTipo = ""; listaTipoAbierta = false }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.opcion_otro_tipo)) },
                            onClick = { tipoElegido = TIPO_OTRO; valorTipo = ""; listaTipoAbierta = false }
                        )
                    }
                }

                // Transformador: solo el número, los kVA se agregan solos
                if (esTransformador) {
                    CampoTexto(
                        valor = valorTipo,
                        onValorCambia = { valorTipo = it.filter { c -> c.isDigit() }.take(5) },
                        etiqueta = stringResource(R.string.campo_potencia_transformador),
                        error = errorValorTipo,
                        tipoTeclado = KeyboardType.Number
                    )
                }

                // Otro tipo: nombre + valor + unidad elegida de una lista corta
                if (esOtroTipo) {
                    CampoTexto(
                        valor = nombreTipo,
                        onValorCambia = { nombreTipo = it },
                        etiqueta = stringResource(R.string.campo_nombre_tipo),
                        error = errorNombreTipo
                    )
                    CampoTexto(
                        valor = valorTipo,
                        onValorCambia = { valorTipo = it.filter { c -> c.isDigit() }.take(5) },
                        etiqueta = stringResource(R.string.campo_valor_tipo),
                        error = errorValorTipo,
                        tipoTeclado = KeyboardType.Number
                    )
                    Text(
                        text = stringResource(R.string.etiqueta_unidad_tipo),
                        style = MaterialTheme.typography.titleSmall
                    )
                    GrupoRadio(
                        opciones = unidadesTipo,
                        seleccionada = unidadTipo,
                        onSeleccionar = { unidadTipo = it },
                        textoDe = { it }
                    )
                }
                if (tipoFinal.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.tipo_se_guardara, tipoFinal),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }


            TarjetaSeccion(titulo = stringResource(R.string.seccion_empresa_equipo)) {
                Text(
                    text = stringResource(R.string.etiqueta_empresa_equipo),
                    style = MaterialTheme.typography.titleSmall
                )
                if (empresasConClientes.isEmpty()) {
                    Text(
                        text = stringResource(R.string.sin_empresas_con_clientes),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    GrupoRadio(
                        opciones = empresasConClientes.map { it.idEmpresa },
                        seleccionada = idEmpresa,
                        // al cambiar de empresa se vuelve a elegir cliente e instalación
                        onSeleccionar = {
                            idEmpresa = it; idCliente = 0L
                            instalacion = ""; escribirOtra = false; nombrePlanta = ""; numeroSE = ""
                        },
                        textoDe = { id -> empresasConClientes.first { it.idEmpresa == id }.nombre }
                    )
                }
                if (errorEmpresa != null) {
                    Text(errorEmpresa, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                if (idEmpresa != 0L) {
                    // Dirección automática: la de la empresa elegida
                    Text(
                        text = stringResource(R.string.etiqueta_direccion_equipo),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = if (direccionEmpresa.isNotBlank()) "📍 $direccionEmpresa" else stringResource(R.string.empresa_sin_direccion),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = stringResource(R.string.etiqueta_cliente_equipo),
                        style = MaterialTheme.typography.titleSmall
                    )
                    GrupoRadio(
                        opciones = clientesDeLaEmpresa.map { it.idUsuario },
                        seleccionada = idCliente,
                        onSeleccionar = { idCliente = it },
                        textoDe = { id ->
                            val c = clientesDeLaEmpresa.first { it.idUsuario == id }
                            "${c.nombre} ${c.apellido}".trim()
                        }
                    )
                    if (errorCliente != null) {
                        Text(errorCliente, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }


            TarjetaSeccion(titulo = stringResource(R.string.seccion_instalacion_equipo)) {
                if (idEmpresa == 0L) {
                    Text(
                        text = stringResource(R.string.indicacion_elegir_empresa_primero),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    // Lista desplegable con las instalaciones que ya tiene esa empresa
                    if (instalacionesDisponibles.isNotEmpty()) {
                        ExposedDropdownMenuBox(
                            expanded = listaAbierta,
                            onExpandedChange = { listaAbierta = it }
                        ) {
                            OutlinedTextField(
                                value = if (escribirOtra) stringResource(R.string.opcion_otra_instalacion) else instalacion,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.campo_instalacion_equipo)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = listaAbierta) },
                                isError = errorInstalacion != null,
                                supportingText = { if (errorInstalacion != null) Text(errorInstalacion) },
                                shape = EstilosCampo.forma,
                                colors = EstilosCampo.colores(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = listaAbierta,
                                onDismissRequest = { listaAbierta = false }
                            ) {
                                instalacionesDisponibles.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = { instalacion = opcion; escribirOtra = false; listaAbierta = false }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.opcion_otra_instalacion)) },
                                    onClick = { instalacion = ""; escribirOtra = true; listaAbierta = false }
                                )
                            }
                        }
                    }
                    // Si la empresa no tiene instalaciones todavía, o eligió "Otra", escribe una nueva
                    if (escribirNueva) {
                        CampoTexto(
                            valor = nombrePlanta,
                            onValorCambia = { nombrePlanta = it },
                            etiqueta = stringResource(R.string.campo_nombre_planta),
                            error = errorPlanta
                        )
                        CampoTexto(
                            valor = numeroSE,
                            onValorCambia = { numeroSE = it.filter { c -> c.isDigit() }.take(2) },
                            etiqueta = stringResource(R.string.campo_numero_se),
                            error = errorNumeroSE,
                            tipoTeclado = KeyboardType.Number
                        )
                        if (instalacionFinal.isNotBlank()) {
                            Text(
                                text = stringResource(R.string.instalacion_se_guardara, instalacionFinal),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }


            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_equipo),
                onClick = { guardarEquipo() },
                icono = R.drawable.ic_check,
                habilitado = !guardando,          // siempre activo: así se ven los errores al apretar
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AgregarEquipoPreview() {
    InduztekTheme {
        AgregarEquipoScreen(
            equipos = emptyList(),
            empresas = listOf(EmpresaEntity(idEmpresa = 2, nombre = "Industrias Aurora S.A.", direccion = "Camino Los Maitenes 1200, Lampa")),
            clientes = listOf(UsuarioEntity(idUsuario = 2, nombre = "Pedro", apellido = "Salas", correo = "cliente@gmail.cl", contrasena = "123456", idRol = 2, idEmpresa = 2)),
            onGuardar = { _, _, _, _, _, _, _, _, _ -> },
            onVolver = {}
        )
    }
}