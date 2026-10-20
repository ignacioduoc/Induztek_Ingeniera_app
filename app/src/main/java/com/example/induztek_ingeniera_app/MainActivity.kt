package com.example.induztek_ingeniera_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.induztek_ingeniera_app.navigation.AppNavegacion
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

// =====================================================================
//  MainActivity.kt  —  PUNTO DE ENTRADA DE LA APP
//  ---------------------------------------------------------------------
//      setContent {
//          Tema {              <- colores / tipografía / formas
//              AppNavegacion() <- el NavHost decide qué pantalla mostrar
//          }
//      }
// =====================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja de borde a borde; el Scaffold respeta las barras del sistema
        setContent {
            InduztekTheme {
                AppNavegacion()
            }
        }
    }
}