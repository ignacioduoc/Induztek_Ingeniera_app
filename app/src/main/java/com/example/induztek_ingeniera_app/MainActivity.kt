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
import com.example.induztek_ingeniera_app.viewmodel.AdminEmpresasViewModel
import com.example.induztek_ingeniera_app.viewmodel.AdminEquiposViewModel
import com.example.induztek_ingeniera_app.viewmodel.AdminUsuariosViewModel
import com.example.induztek_ingeniera_app.viewmodel.LoginViewModel
import com.example.induztek_ingeniera_app.viewmodel.PerfilViewModel
import com.example.induztek_ingeniera_app.viewmodel.ProtocolosViewModel
import com.example.induztek_ingeniera_app.viewmodel.RestablecerContrasenaViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)


        val repository = ProtocoloRepository(
            usuarioDao = database.usuarioDao(),
            protocoloDao = database.protocoloDao(),
            equipoDao = database.equipoDao()
        )


        val viewModelFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                        LoginViewModel(repository) as T
                    modelClass.isAssignableFrom(ProtocolosViewModel::class.java) ->
                        ProtocolosViewModel(repository) as T

                    modelClass.isAssignableFrom(RestablecerContrasenaViewModel::class.java) ->
                        RestablecerContrasenaViewModel(repository) as T

                    modelClass.isAssignableFrom(AdminUsuariosViewModel::class.java) ->
                        AdminUsuariosViewModel(repository) as T

                    modelClass.isAssignableFrom(AdminEmpresasViewModel::class.java) ->
                        AdminEmpresasViewModel(repository) as T

                    modelClass.isAssignableFrom(AdminEquiposViewModel::class.java) ->
                        AdminEquiposViewModel(repository) as T

                    modelClass.isAssignableFrom(PerfilViewModel::class.java) ->
                        PerfilViewModel(repository) as T

                    else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
                }
            }
        }

        setContent {
            InduztekTheme {

                val loginViewModel: LoginViewModel = viewModel(factory = viewModelFactory)
                val protocolosViewModel: ProtocolosViewModel = viewModel(factory = viewModelFactory)
                val restablecerViewModel: RestablecerContrasenaViewModel = viewModel(factory = viewModelFactory)
                val adminUsuariosViewModel: AdminUsuariosViewModel = viewModel(factory = viewModelFactory)
                val adminEmpresasViewModel: AdminEmpresasViewModel = viewModel(factory = viewModelFactory)
                val adminEquiposViewModel: AdminEquiposViewModel = viewModel(factory = viewModelFactory)
                val perfilViewModel: PerfilViewModel = viewModel(factory = viewModelFactory)


                AppNavegacion(
                    loginViewModel = loginViewModel,
                    protocolosViewModel = protocolosViewModel,
                    restablecerViewModel = restablecerViewModel,
                    adminUsuariosViewModel = adminUsuariosViewModel,
                    adminEmpresasViewModel = adminEmpresasViewModel,
                    adminEquiposViewModel = adminEquiposViewModel,
                    perfilViewModel = perfilViewModel
                )
            }
        }
    }
}