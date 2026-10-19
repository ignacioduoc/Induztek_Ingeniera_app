package com.example.induztek_ingeniera_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// =====================================================================
//  Theme.kt  —  EL TEMA GLOBAL (equivale a la hoja de estilos principal)
//  ---------------------------------------------------------------------
//  MaterialTheme "envuelve" toda la app. Cualquier componente de
//  Material 3 (Button, Text, Card...) toma sus colores, letras y formas
//  de aquí AUTOMÁTICAMENTE. Esto es el "estilo por defecto".
//
//  NOTA: la plantilla de Android Studio trae "dynamicColor = true", que en
//  Android 12+ reemplaza la paleta por los colores del fondo de pantalla del
//  celular. Lo quitamos para que SIEMPRE se vea nuestro diseño propio.
//
//  Si cambias "primary" en EsquemaClaro, cambian TODOS los Button de la
//  app sin tocar las pantallas. Esa es la gracia de centralizar el diseño.
// =====================================================================

// Colores para modo claro: asignamos NUESTRA paleta a los roles de Material
private val EsquemaClaro = lightColorScheme(
    primary = Primario,              // Fondo de Button
    onPrimary = Blanco,              // Texto sobre primary
    primaryContainer = PrimarioClaro, // Fondo de FilledTonalButton
    onPrimaryContainer = PrimarioOscuro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro
)

// Colores para modo oscuro (se activa según la configuración del teléfono)
private val EsquemaOscuro = darkColorScheme(
    primary = PrimarioClaro,
    onPrimary = PrimarioOscuro,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = PrimarioClaro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro
)

// Formas (equivale a border-radius). Los Button usan por defecto la forma "full" (píldora).
private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

/**
 * Tema de la aplicación. Se usa UNA vez en MainActivity envolviendo todo el contenido.
 * @param darkTheme true para modo oscuro. Por defecto sigue la configuración del sistema.
 */
@Composable
fun InduztekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}