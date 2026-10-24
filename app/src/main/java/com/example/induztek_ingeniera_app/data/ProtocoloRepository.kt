package com.example.induztek_ingeniera_app.data

import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * REPOSITORIO
 * Es la única fuente de datos de la app. Hoy los protocolos vienen de SQLite
 * (ProtocoloDbHelper) y la sesión de SharedPreferences (SesionPreferencias);
 * más adelante podrían venir de un microservicio sin que el ViewModel ni la
 * vista tengan que cambiar.
 */
class ProtocoloRepository(
    private val usuarioDao: UsuarioDao,
    private val protocoloDao: ProtocoloDao
) {
    // Autenticación de sesiones de usuario
    suspend fun iniciarSesion(correo: String, contrasena: String): UsuarioEntity? {
        return usuarioDao.iniciarSesion(correo, contrasena)
    }

    suspend fun buscarUsuarioPorId(id: Long): UsuarioEntity? {
        return usuarioDao.buscarUsuarioPorId(id)
    }

    // Escritura de Órdenes de Trabajo (Soporta flujos asíncronos nativos suspend)
    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long {
        return protocoloDao.insertarProtocolo(protocolo)
    }

    suspend fun formalizarProtocolo(id: Long): Boolean {
        return protocoloDao.formalizarProtocolo(id) > 0
    }

    // Lecturas Reactivas en Tiempo Real para tus componentes Compose
    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeCliente(clienteId)
    }

    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeEquipo(equipoId)
    }

    suspend fun buscarProtocoloPorId(id: Long): ProtocoloConDetalles? {
        return protocoloDao.buscarProtocoloPorId(id)
    }
}