package com.example.induztek_ingeniera_app.data

import com.example.induztek_ingeniera_app.data.dao.EquipoDao
import com.example.induztek_ingeniera_app.data.dao.ProtocoloDao
import com.example.induztek_ingeniera_app.data.dao.UsuarioDao
import com.example.induztek_ingeniera_app.data.entities.EquipoEntity
import com.example.induztek_ingeniera_app.data.entities.ProtocoloConDetalles
import com.example.induztek_ingeniera_app.data.entities.ProtocoloEntity
import com.example.induztek_ingeniera_app.data.entities.UsuarioEntity
import com.example.induztek_ingeniera_app.model.Rol
import com.example.induztek_ingeniera_app.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class ProtocoloRepository(
    private val usuarioDao: UsuarioDao,
    private val protocoloDao: ProtocoloDao,
    private val equipoDao: EquipoDao
) {
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val entity = usuarioDao.iniciarSesion(correo, contrasena)
        return entity?.toDomainModel()
    }

    suspend fun buscarUsuarioPorId(id: Long): Usuario? {
        val entity = usuarioDao.buscarUsuarioPorId(id).firstOrNull()
        return entity?.toDomainModel()
    }

    suspend fun cambiarContrasena(correo: String, nueva: String): Boolean {
        return usuarioDao.actualizarContrasena(correo, nueva) > 0
    }

    suspend fun actualizarPerfilUsuario(idUsuario: Long, nombre: String, correo: String, fotoPerfil: ByteArray?) {
        usuarioDao.actualizarPerfil(idUsuario, nombre, correo, fotoPerfil)
    }


    fun obtenerTodosLosEquipos(): Flow<List<EquipoEntity>> {
        return equipoDao.obtenerTodos()
    }

    fun obtenerEquiposPorCliente(idCliente: Long): Flow<List<EquipoEntity>> {
        return equipoDao.obtenerEquiposPorCliente(idCliente)
    }

    fun buscarEquipoPorId(idEquipo: Long): Flow<EquipoEntity?> {
        return equipoDao.buscarEquipoPorId(idEquipo)
    }

    suspend fun actualizarUbicacionEquipo(idEquipo: Long, direccion: String, latitud: Double, longitud: Double) {
        equipoDao.actualizarUbicacion(idEquipo, direccion, latitud, longitud)
    }

    suspend fun insertarProtocolo(protocolo: ProtocoloEntity): Long {
        return protocoloDao.insertarProtocolo(protocolo)
    }

    suspend fun formalizarProtocolo(id: Long): Boolean {
        return protocoloDao.formalizarProtocolo(id) > 0
    }

    fun listarProtocolosDeCliente(clienteId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeCliente(clienteId)
    }

    fun listarProtocolosDeEquipo(equipoId: Long): Flow<List<ProtocoloConDetalles>> {
        return protocoloDao.listarProtocolosDeEquipo(equipoId)
    }

    suspend fun buscarProtocoloPorId(id: Long): ProtocoloConDetalles? {
        return protocoloDao.buscarProtocoloPorId(id).firstOrNull()
    }

    private fun UsuarioEntity.toDomainModel(): Usuario {
        val rolNombre = when (this.idRol) {
            Rol.ID_TECNICO -> "Técnico"
            Rol.ID_CLIENTE -> "Cliente"
            else -> "Usuario"
        }
        return Usuario(
            id = this.idUsuario,
            nombre = this.nombre,
            correo = this.correo,
            empresa = this.empresa,
            rolId = this.idRol,
            rolNombre = rolNombre,
            fotoPerfil = this.fotoPerfil
        )
    }
}