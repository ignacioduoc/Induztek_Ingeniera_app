package com.example.induztek_ingeniera_app.ui.screens

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.induztek_ingeniera_app.R
import com.example.induztek_ingeniera_app.ui.components.BotonPrincipal
import com.example.induztek_ingeniera_app.ui.components.BotonSecundario
import com.example.induztek_ingeniera_app.ui.components.CampoTexto
import com.example.induztek_ingeniera_app.ui.components.TarjetaSeccion
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.utils.leerImagenComoPng
import com.example.induztek_ingeniera_app.viewmodel.AdminEmpresasViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEmpresaFormScreen(
    viewModel: AdminEmpresasViewModel,
    onVolver: () -> Unit
) {
    val f by viewModel.formulario.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cuando el ViewModel avisa que se guardó: mensaje, se limpia la señal y se vuelve a la lista
    LaunchedEffect(f.guardado) {
        if (f.guardado) {
            Toast.makeText(context, context.getString(R.string.empresa_guardada), Toast.LENGTH_SHORT).show()
            viewModel.limpiarGuardado()
            onVolver()
        }
    }

    // Galería: se elige la imagen y se deja achicada (PNG) dentro del formulario
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = leerImagenComoPng(context, uri)
            if (bytes != null) {
                viewModel.onLogoCambia(bytes)
            } else {
                Toast.makeText(context, context.getString(R.string.error_cargar_logo), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val bitmapLogo = remember(f.logo) {
        f.logo?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (f.esEdicion) R.string.titulo_editar_empresa else R.string.titulo_nueva_empresa
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

            TarjetaSeccion(titulo = stringResource(R.string.seccion_datos_empresa)) {
                CampoTexto(
                    valor = f.nombre,
                    onValorCambia = viewModel::onNombreCambia,
                    etiqueta = stringResource(R.string.campo_nombre_empresa),
                    error = f.errorNombre
                )
                CampoTexto(
                    valor = f.direccion,
                    onValorCambia = viewModel::onDireccionCambia,
                    etiqueta = stringResource(R.string.campo_direccion_empresa),
                    error = f.errorDireccion
                )
            }

            TarjetaSeccion(titulo = stringResource(R.string.seccion_logo_empresa)) {
                Text(
                    text = stringResource(R.string.indicacion_logo_empresa),
                    style = MaterialTheme.typography.bodySmall
                )

                // Vista previa del logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmapLogo != null) {
                        Image(
                            bitmap = bitmapLogo.asImageBitmap(),
                            contentDescription = stringResource(R.string.cd_logo_empresa),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(8.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)) {
                    BotonSecundario(
                        texto = stringResource(
                            if (f.logo == null) R.string.btn_elegir_logo else R.string.btn_cambiar_logo
                        ),
                        onClick = { launcherGaleria.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    )
                    if (f.logo != null) {
                        BotonSecundario(
                            texto = stringResource(R.string.btn_quitar_logo),
                            onClick = { viewModel.onLogoCambia(null) },
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                texto = stringResource(R.string.btn_guardar_empresa),
                onClick = viewModel::onGuardar,
                icono = R.drawable.ic_check,
                habilitado = !f.guardando && !f.cargando,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}