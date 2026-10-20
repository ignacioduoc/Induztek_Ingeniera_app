package com.example.induztek_ingeniera_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.induztek_ingeniera_app.data.ProtocoloRepository
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Rol
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
//  Guarda quién inició sesión, la lista de equipos y la lista de
//  protocolos del cliente. Después de cada cambio en la BD (INSERT del
//  técnico, UPDATE del cliente) RECARGA la lista, igual que el profe.
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

    // Protocolos de los equipos del cliente con sesión ("Mis protocolos"),
    // del más nuevo al más antiguo.
    private val _protocolos = MutableStateFlow(protocolosDe(_usuarioActual.value))
    val protocolos: StateFlow<List<Protocolo>> = _protocolos.asStateFlow()

    // Vuelve a leer la lista desde la BD (igual que recargar() del profe).
    // Se llama después de cada cambio: ingreso, INSERT, UPDATE y cierre de sesión.
    private fun recargar() {
        _protocolos.value = protocolosDe(_usuarioActual.value)
    }

    // Solo el cliente tiene "Mis protocolos". Sin sesión o para el técnico: lista vacía.
    private fun protocolosDe(usuario: Usuario?): List<Protocolo> =
        if (usuario != null && usuario.rolId == Rol.ID_CLIENTE) repositorio.protocolosDeCliente(usuario.id)
        else emptyList()

    // Lo llama AppNavegacion cuando el login fue exitoso.
    // (La sesión ya la guardó el Repository al comprobar el usuario.)
    fun registrarIngreso(usuario: Usuario) {
        _usuarioActual.value = usuario
        recargar()
    }

    // Para el formulario: el equipo que se va a probar (READ por su id)
    fun obtenerEquipo(id: Long): Equipo? = repositorio.buscarEquipo(id)

    // INSERT: el protocolo nuevo que armó el formulario
    fun guardarProtocolo(protocolo: Protocolo) {
        repositorio.guardarProtocolo(protocolo)
        recargar()
    }

    // Para el detalle: el protocolo tocado en la lista (READ por su id, con JOIN)
    fun obtenerProtocolo(id: Long): Protocolo? = repositorio.buscarProtocolo(id)

    // UPDATE: el cliente da su conformidad y el estado pasa a FORMALIZADO.
    // Después se recarga la lista para que el chip cambie de color.
    fun formalizar(id: Long) {
        repositorio.formalizar(id)
        recargar()
    }

    fun cerrarSesion() {
        repositorio.cerrarSesion()
        _usuarioActual.value = null
        recargar()
    }

    // Al destruirse el ViewModel se cierra la conexión con la BD
    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}