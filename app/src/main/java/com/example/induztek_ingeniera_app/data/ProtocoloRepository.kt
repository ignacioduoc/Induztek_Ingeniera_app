package com.example.induztek_ingeniera_app.data

import android.content.Context
import com.example.induztek_ingeniera_app.model.Equipo
import com.example.induztek_ingeniera_app.model.Protocolo
import com.example.induztek_ingeniera_app.model.Usuario

/**
 * REPOSITORIO
 * Es la única fuente de datos de la app. Hoy los protocolos vienen de SQLite
 * (ProtocoloDbHelper) y la sesión de SharedPreferences (SesionPreferencias);
 * más adelante podrían venir de un microservicio sin que el ViewModel ni la
 * vista tengan que cambiar.
 */
class ProtocoloRepository(context: Context) {

    // Acceso a las tablas (SQLite)
    private val db = ProtocoloDbHelper(context)

    // Acceso a la sesión guardada (SharedPreferences)
    private val sesion = SesionPreferencias(context)

    // ----------------------------- SESIÓN ----------------------------

    // Busca al usuario en la BD. Si existe, guarda su id para recordar la sesión.
    // Devuelve el usuario, o null si el correo o la contraseña no coinciden.
    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val usuario = db.iniciarSesion(correo, contrasena)
        if (usuario != null) {
            sesion.guardar(usuario.id)
        }
        return usuario
    }

    // Al abrir la app: el usuario que dejó la sesión abierta (null = no hay sesión).
    // Si no hay id guardado, leer() devuelve -1 y la BD no encuentra a nadie.
    fun usuarioConSesion(): Usuario? = db.buscarUsuarioPorId(sesion.leer())

    fun cerrarSesion() {
        sesion.borrar()
    }

    // ----------------------------- EQUIPOS ---------------------------

    fun listarEquipos(): List<Equipo> = db.listarEquipos()

    fun buscarEquipo(id: Long): Equipo? = db.buscarEquipoPorId(id)

    // ---------------------------- PROTOCOLOS -------------------------

    // Devuelve el id del protocolo nuevo (o -1 si no se pudo guardar)
    fun guardarProtocolo(protocolo: Protocolo): Long = db.insertarProtocolo(protocolo)

    fun protocolosDeCliente(clienteId: Long): List<Protocolo> =
        db.listarProtocolosDeCliente(clienteId)

    fun historialDeEquipo(equipoId: Long): List<Protocolo> =
        db.listarProtocolosDeEquipo(equipoId)

    fun buscarProtocolo(id: Long): Protocolo? = db.buscarProtocoloPorId(id)

    // Devuelve cuántas filas cambió (1 = formalizado)
    fun formalizar(id: Long): Int = db.formalizarProtocolo(id)

    // Cierra la conexión con la BD (lo llama el ViewModel cuando se destruye)
    fun cerrar() {
        db.close()
    }
}