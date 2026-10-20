package com.example.induztek_ingeniera_app.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.induztek_ingeniera_app.ui.theme.Dimens
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

// =====================================================================
//  FotoEvidencia.kt  —  Foto del equipo con esquinas redondeadas y borde.
//  ---------------------------------------------------------------------
//  Adaptado de AvatarCircular del profe: la misma técnica, pero la foto
//  ocupa todo el ancho y tiene esquinas redondeadas en vez de círculo.
//  En CSS:  img { width:100%; height:180px; border-radius:12px;
//                 border:4px solid white; object-fit:cover; }
//  ¡El ORDEN de los modificadores importa! (se aplican de arriba a abajo)
//     fillMaxWidth + height -> tamaño (todo el ancho, 180 dp de alto)
//     shadow                -> sombra con la misma forma
//     clip                  -> recorta las esquinas
//     border                -> dibuja el borde encima del recorte
// =====================================================================

@Composable
fun FotoEvidencia(
    foto: ImageBitmap,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    // Forma "medium" del tema: esquinas de 12 dp (ver Formas en Theme.kt)
    val forma = MaterialTheme.shapes.medium

    Image(
        // BitmapPainter -> dibuja una imagen que está en memoria (la foto tomada)
        painter = BitmapPainter(foto),
        contentDescription = descripcion,
        // Crop: llena el espacio sin deformar la foto; recorta lo que sobra
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.altoFoto)
            .shadow(elevation = 6.dp, shape = forma)
            .clip(forma)
            .border(Dimens.bordeFoto, MaterialTheme.colorScheme.surface, forma)
    )
}

@Preview(showBackground = true)
@Composable
private fun FotoEvidenciaPreview() {
    // En la vista previa no hay cámara: se usa una imagen gris de 400 x 300 px
    // solo para ver el tamaño, las esquinas, el borde y la sombra.
    val fotoEjemplo = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        .apply { eraseColor(Color.LTGRAY) }
        .asImageBitmap()

    InduztekTheme {
        FotoEvidencia(foto = fotoEjemplo, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}