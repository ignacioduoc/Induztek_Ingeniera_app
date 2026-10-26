package com.example.induztek_ingeniera_app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.induztek_ingeniera_app.model.Usuario
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Función de seguridad para redimensionar y comprimir Bitmaps antes de guardar en SQLite (BLOB).
 */
private fun comprimirYRedimensionarBitmap(bitmapOriginal: Bitmap, maxDim: Int = 512): ByteArray {
    val width = bitmapOriginal.width
    val height = bitmapOriginal.height
    val ratio = width.toFloat() / height.toFloat()

    val newWidth: Int
    val newHeight: Int
    if (width > height) {
        newWidth = maxDim
        newHeight = (maxDim / ratio).toInt()
    } else {
        newHeight = maxDim
        newWidth = (maxDim * ratio).toInt()
    }

    val scaledBitmap = Bitmap.createScaledBitmap(bitmapOriginal, newWidth, newHeight, true)

    val softwareBitmap = try {
        scaledBitmap.copy(Bitmap.Config.ARGB_8888, false) ?: scaledBitmap
    } catch (e: Exception) {
        scaledBitmap
    }

    val stream = ByteArrayOutputStream()
    softwareBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return stream.toByteArray()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    usuario: Usuario?,
    onGuardar: (nombre: String, correo: String, fotoPerfil: ByteArray?) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var nombreState by remember(usuario) { mutableStateOf(usuario?.nombre ?: "") }
    var correoState by remember(usuario) { mutableStateOf(usuario?.correo ?: "") }
    var bitmapFoto by remember(usuario) {
        mutableStateOf<Bitmap?>(
            usuario?.fotoPerfil?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        )
    }
    var mostrarDialogoOrigen by remember { mutableStateOf(false) }
    var photoUriTemp by remember { mutableStateOf<Uri?>(null) }

    // Función auxiliar para crear la URI temporal de la foto capturada
    fun crearUriFoto(): Uri? {
        return try {
            val file = File.createTempFile("avatar_capture_", ".jpg", context.cacheDir)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            null
        }
    }

    // Launcher para tomar foto de alta resolución con URI
    val launcherCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { exito: Boolean ->
        if (exito && photoUriTemp != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, photoUriTemp!!)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, photoUriTemp)
                }
                bitmapFoto = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Error al procesar la captura", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher para solicitar el permiso de Cámara en Runtime
    val launcherPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val uri = crearUriFoto()
            if (uri != null) {
                photoUriTemp = uri
                launcherCamara.launch(uri)
            } else {
                Toast.makeText(context, "Error al crear archivo de foto", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher para seleccionar foto desde la Galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                bitmapFoto = bitmap
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Diálogo emergente
    if (mostrarDialogoOrigen) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOrigen = false },
            title = { Text("Foto de Perfil") },
            text = { Text("Selecciona el origen de la imagen:") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    val permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    if (permiso == PackageManager.PERMISSION_GRANTED) {
                        val uri = crearUriFoto()
                        if (uri != null) {
                            photoUriTemp = uri
                            launcherCamara.launch(uri)
                        } else {
                            Toast.makeText(context, "Error al preparar archivo de foto", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        launcherPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    mostrarDialogoOrigen = false
                    launcherGaleria.launch("image/*")
                }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Perfil") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .size(130.dp)
                    .clickable { mostrarDialogoOrigen = true }
            ) {
                if (bitmapFoto != null) {
                    Image(
                        bitmap = bitmapFoto!!.asImageBitmap(),
                        contentDescription = "Foto de Perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = usuario?.nombre?.take(1)?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Cambiar Foto",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                    )
                }
            }

            OutlinedTextField(
                value = nombreState,
                onValueChange = { nombreState = it },
                label = { Text("Nombre Completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correoState,
                onValueChange = { correoState = it },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = usuario?.empresa ?: "",
                onValueChange = {},
                enabled = false,
                label = { Text("Empresa") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (nombreState.isNotBlank() && correoState.isNotBlank()) {
                        val byteArrayFoto = bitmapFoto?.let { bitmap ->
                            comprimirYRedimensionarBitmap(bitmap)
                        }
                        onGuardar(nombreState, correoState, byteArrayFoto)
                        Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                        onVolver()
                    } else {
                        Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}