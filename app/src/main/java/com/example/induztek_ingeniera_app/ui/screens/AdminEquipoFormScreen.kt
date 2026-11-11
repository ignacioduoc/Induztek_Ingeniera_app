package com.example.induztek_ingeniera_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.ListaDesplegable
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.utils.Validaciones
import com.example.induztek_ingeniera_app.ui.utils.buscarCoordenadasGratis
import com.example.induztek_ingeniera_app.viewmodel.AdminEquiposViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEquipoFormScreen(
    viewModel: AdminEquiposViewModel,
    onVolver: () -> Unit
) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()
    val empresas by viewModel.empresas.collectAsStateWithLifecycle()
    val clientes by viewModel.clientes.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cuando el ViewModel avisa que se guardó: mensaje, se limpia la señal y se vuelve a la lista
    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            Toast.makeText(context, context.getString(R.string.equipo_guardado), Toast.LENGTH_SHORT).show()
            viewModel.limpiarGuardado()
            onVolver()
        }
    }

    val textoOtroTipo = stringResource(R.string.opcion_otro_tipo)
    val clientesDeLaEmpresa = clientes.filter { it.idEmpresa == f.idEmpresa }
    val direccionEmpresa = empresas.firstOrNull { it.idEmpresa == f.idEmpresa }?.direccion.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (f.esEdicion) R.string.titulo_editar_equipo else R.string.titulo_agregar_equipo
                        )
                    )
                },
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
                    valor = f.codigo,
                    onValorCambia = viewModel::onCodigoCambia,
                    etiqueta = stringResource(R.string.campo_codigo_equipo),
                    error = f.errorCodigo,
                    tipoTeclado = KeyboardType.Number,
                    prefijo = Validaciones.PREFIJO_CODIGO
                )

                ListaDesplegable(
                    etiqueta = stringResource(R.string.campo_tipo_equipo),
                    textoSeleccionado = when (f.tipoElegido) {
                        Validaciones.TIPO_TRANSFORMADOR -> Validaciones.TIPO_TRANSFORMADOR
                        Validaciones.TIPO_OTRO -> textoOtroTipo
                        else -> ""
                    },
                    opciones = listOf(Validaciones.TIPO_TRANSFORMADOR, Validaciones.TIPO_OTRO),
                    textoDe = { if (it == Validaciones.TIPO_OTRO) textoOtroTipo else it },
                    onElegir = viewModel::onTipoCambia,
                    error = f.errorTipo
                )

                // Transformador: solo el número, los kVA se agregan solos
                if (f.tipoElegido == Validaciones.TIPO_TRANSFORMADOR) {
                    CampoTexto(
                        valor = f.valorTipo,
                        onValorCambia = viewModel::onValorTipoCambia,
                        etiqueta = stringResource(R.string.campo_potencia_transformador),
                        error = f.errorValorTipo,
                        tipoTeclado = KeyboardType.Number
                    )
                }

                // Otro tipo: nombre + valor + unidad elegida de una lista corta
                if (f.tipoElegido == Validaciones.TIPO_OTRO) {
                    CampoTexto(
                        valor = f.nombreTipo,
                        onValorCambia = viewModel::onNombreTipoCambia,
                        etiqueta = stringResource(R.string.campo_nombre_tipo),
                        error = f.errorNombreTipo
                    )
                    CampoTexto(
                        valor = f.valorTipo,
                        onValorCambia = viewModel::onValorTipoCambia,
                        etiqueta = stringResource(R.string.campo_valor_tipo),
                        error = f.errorValorTipo,
                        tipoTeclado = KeyboardType.Number
                    )
                    ListaDesplegable(
                        etiqueta = stringResource(R.string.etiqueta_unidad_tipo),
                        textoSeleccionado = f.unidadTipo,
                        opciones = Validaciones.UNIDADES_TIPO,
                        textoDe = { it },
                        onElegir = viewModel::onUnidadCambia
                    )
                }

                if (f.tipoFinal.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.tipo_se_guardara, f.tipoFinal),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                CampoTexto(
                    valor = f.instalacion,
                    onValorCambia = viewModel::onInstalacionCambia,
                    etiqueta = stringResource(R.string.campo_instalacion_ejemplo),
                    error = f.errorInstalacion
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_empresa_equipo)) {
                ListaDesplegable(
                    etiqueta = stringResource(R.string.etiqueta_empresa_equipo),
                    textoSeleccionado = empresas.firstOrNull { it.idEmpresa == f.idEmpresa }?.nombre ?: "",
                    opciones = empresas,
                    textoDe = { it.nombre },
                    onElegir = { viewModel.onEmpresaCambia(it.idEmpresa) },
                    error = f.errorEmpresa
                )

                // Solo aparecen los clientes de la empresa elegida
                if (f.idEmpresa != 0L) {
                    ListaDesplegable(
                        etiqueta = stringResource(R.string.etiqueta_cliente_equipo),
                        textoSeleccionado = clientesDeLaEmpresa
                            .firstOrNull { it.idUsuario == f.idCliente }
                            ?.let { "${it.nombre} ${it.apellido}".trim() } ?: "",
                        opciones = clientesDeLaEmpresa,
                        textoDe = { "${it.nombre} ${it.apellido}".trim() },
                        onElegir = { viewModel.onClienteCambia(it.idUsuario) },
                        error = f.errorCliente
                    )

                    // La dirección del equipo es la de la empresa (se coloca sola)
                    Text(
                        text = if (direccionEmpresa.isNotBlank()) {
                            stringResource(R.string.etiqueta_direccion_equipo) + ": " + direccionEmpresa
                        } else {
                            stringResource(R.string.empresa_sin_direccion)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (f.errorGeneral != null) {
                Text(
                    text = f.errorGeneral!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BotonPrincipal(
                texto = stringResource(R.string.btn_guardar_equipo),
                onClick = {
                    // Se le pasa al ViewModel la búsqueda de coordenadas (el Geocoder necesita el Context)
                    viewModel.onGuardar { direccion ->
                        var punto: Pair<Double, Double>? = null
                        buscarCoordenadasGratis(context, direccion) { p ->
                            punto = p?.let { it.latitude to it.longitude }
                        }
                        punto
                    }
                },
                icono = R.drawable.ic_check,
                habilitado = !f.guardando && !f.cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}