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


@Composable
fun FotoEvidencia(
    foto: ImageBitmap,
    descripcion: String,
    modifier: Modifier = Modifier
) {

    val forma = MaterialTheme.shapes.medium

    Image(
        painter = BitmapPainter(foto),
        contentDescription = descripcion,
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
    val fotoEjemplo = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888)
        .apply { eraseColor(Color.LTGRAY) }
        .asImageBitmap()

    InduztekTheme {
        FotoEvidencia(foto = fotoEjemplo, descripcion = "", modifier = Modifier.padding(16.dp))
    }
}