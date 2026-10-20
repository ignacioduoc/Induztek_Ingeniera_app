package com.example.induztek_ingeniera_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// =====================================================================
//  ProtocolosViewModel.kt  —  EL VIEWMODEL COMPARTIDO DE LA APP
//  ---------------------------------------------------------------------
//  Igual que el UsuariosViewModel del profe: se crea en AppNavegacion,
//  FUERA del NavHost. Así sobrevive a los cambios de pantalla y todas
//  las pantallas ven los mismos datos.
//  Guarda quién inició sesión y la lista de equipos; en los próximos
//  pasos se le suman los protocolos.
// =====================================================================

class ProtocolosViewModel(application: Application) : AndroidViewModel(application) {

    // Única puerta a los datos (BD + sesión)
    private val repositorio = ProtocoloRepository(application)

    // Usuario con sesión iniciada. Al abrir la app se busca la sesión guardada:
    // si alguien no cerró sesión, entra directo sin pasar por el login.
    private val _usuarioActual = MutableStateFlow(repositorio.usuarioConSesion())
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // Equipos que el técnico puede probar (con la empresa de cada cliente).
    // Se CARGAN desde la BD, igual que la lista de usuarios del profe.
    private val _equipos = MutableStateFlow(repositorio.listarEquipos())
    val equipos: StateFlow<List<Equipo>> = _equipos.asStateFlow()

    // Lo llama AppNavegacion cuando el login fue exitoso.
    // (La sesión ya la guardó el Repository al comprobar el usuario.)
    fun registrarIngreso(usuario: Usuario) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _usuarioActual.value = null
    }

    // Al destruirse el ViewModel se cierra la conexión con la BD
    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}