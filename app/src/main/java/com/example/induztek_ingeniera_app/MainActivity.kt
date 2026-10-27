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
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel

// =====================================================================
//  MainActivity.kt  —  PUNTO DE ENTRADA DE LA APP
//  ---------------------------------------------------------------------
//  1) Abre la base de datos Room.
//  2) Crea el Repository (la única puerta a los datos) con los DAO.
//  3) Crea la Factory: la "fábrica" que sabe armar los ViewModels que
//     necesitan el Repository.
//  4) Dibuja la app:
//      setContent {
//          Tema {              <- colores / tipografía / formas
//              AppNavegacion() <- el NavHost decide qué pantalla mostrar
//          }
//      }
// =====================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Room: una sola instancia de la BD para toda la app (patrón Singleton)
        val database = AppDatabase.getDatabase(this)

        // 2. El Repository recibe los DAO (una interfaz por tabla)
        val repository = ProtocoloRepository(
            usuarioDao = database.usuarioDao(),
            protocoloDao = database.protocoloDao(),
            equipoDao = database.equipoDao()       // para las direcciones y coordenadas de los equipos
        )

        // 3. La Factory. viewModel() por sí solo SOLO sabe crear ViewModels SIN
        //    parámetros. Los que reciben el Repository se crean aquí, con "when":
        //    "si me piden un LoginViewModel, lo armo con el repository", etc.
        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                        LoginViewModel(repository) as T
                    modelClass.isAssignableFrom(ProtocolosViewModel::class.java) ->
                        ProtocolosViewModel(repository) as T
                    // NUEVO (Paso 16): también usa el Repository (hace el UPDATE de la contraseña)
                    modelClass.isAssignableFrom(RestablecerContrasenaViewModel::class.java) ->
                        RestablecerContrasenaViewModel(repository) as T
                    // ARREGLO (Paso 16): NuevoProtocoloViewModel ya NO va aquí. No usa el
                    // Repository, así que su pantalla lo crea sola con viewModel().
                    else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
                }
            }
        }

        setContent {
            InduztekTheme {
                // 4. Los ViewModels que viven mientras la app está abierta
                val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
                val protocolosViewModel: ProtocolosViewModel = viewModel(factory = viewModelFactory)
                val restablecerViewModel: RestablecerContrasenaViewModel = viewModel(factory = viewModelFactory)

                // 5. La navegación recibe los ViewModels listos para usar Room
                AppNavegacion(
                    loginViewModel = loginViewModel,
                    protocolosViewModel = protocolosViewModel,
                    restablecerViewModel = restablecerViewModel
                )
            }
        }
    }
}