package com.example.induztek_ingeniera_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.induztek_ingeniera_app.ui.screens.LoginScreen
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // dibuja de borde a borde; el Scaffold respeta las barras del sistema
        setContent {
            InduztekTheme {
                // TEMPORAL (Paso 8): en el Paso 9 aquí va AppNavegacion()
                LoginScreen(onLoginExitoso = {})
            }
        }
    }
}