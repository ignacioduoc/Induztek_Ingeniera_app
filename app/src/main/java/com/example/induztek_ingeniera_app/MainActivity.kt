package com.example.induztek_ingeniera_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.induztek_ingeniera_app.data.AppDatabase
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.navigation.AppNavegacion
import com.example.induztek_ingeniera_app.ui.theme.InduztekTheme
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel
import com.example.induztek_ingeniera_app.viewmodel.NuevoProtocoloViewModel
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel

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
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // 1. Inicializar Room mediante nuestro patrón Singleton seguro
        val database = AppDatabase.getDatabase(this)

        // 2. Instanciar el Repositorio inyectando los DAOs requeridos
        val repository = ProtocoloRepository(
            usuarioDao = database.usuarioDao(),
            protocoloDao = database.protocoloDao()
        )

        // 3. Crear la Fábrica (ViewModelProvider.Factory) para inyectar el repositorio en tus ViewModels de MVVM
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                        LoginViewModel(repository) as T
                    modelClass.isAssignableFrom(ProtocolosViewModel::class.java) ->
                        ProtocolosViewModel(repository) as T
                    modelClass.isAssignableFrom(NuevoProtocoloViewModel::class.java) ->
                        NuevoProtocoloViewModel(repository) as T
                    else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
                }
            }
        }

        setContent {
            InduztekTheme {
                // 4. Obtenemos las instancias de los ViewModels configuradas con nuestra Fábrica centralizada
                val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
                val protocolosViewModel: ProtocolosViewModel = viewModel(factory = viewModelFactory)
                val nuevoProtocoloViewModel: NuevoProtocoloViewModel = viewModel(factory = viewModelFactory)

                // 5. Lanzamos tu componente de navegación pasándole los ViewModels listos para operar con Room
                AppNavegacion(
                    loginViewModel = loginViewModel,
                    protocolosViewModel = protocolosViewModel,
                    nuevoProtocoloViewModel = nuevoProtocoloViewModel
                )
            }
        }
    }
}
